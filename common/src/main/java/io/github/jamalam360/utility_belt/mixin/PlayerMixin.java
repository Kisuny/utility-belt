package io.github.jamalam360.utility_belt.mixin;

import io.github.jamalam360.utility_belt.StateManager;
import io.github.jamalam360.utility_belt.UtilityBeltInventory;
import io.github.jamalam360.utility_belt.UtilityBeltItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	/**
	 * @reason Patches setItemSlot to set the item in the belt if the player is switched to it
	 */
	@Inject(
			method = "setItemSlot",
			at = @At("HEAD")
	)
	private void utilitybelt$setItemInHand(EquipmentSlot equipmentSlot, ItemStack itemStack, CallbackInfo ci) {
		if (equipmentSlot == EquipmentSlot.MAINHAND && (Object) this instanceof ServerPlayer player) {
			StateManager stateManager = StateManager.getServerInstance();
			if (stateManager.isInBelt(player)) {
				UtilityBeltInventory inv = stateManager.getInventory(player);
				inv.setItem(stateManager.getSelectedBeltSlot(player), itemStack);
			}
		}
	}

	@Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void utilitybelt$startPlayerTick(CallbackInfo ci) {
		StateManager stateManager = StateManager.getServerInstance();
		ItemStack belt = UtilityBeltItem.getBelt((Player) (Object) this);

		if (belt != null) {
			// Save previous state
			UtilityBeltInventory inv = stateManager.getInventory((Player) (Object) this);
			UtilityBeltInventory nbtInv = UtilityBeltItem.getInventoryFromTag(belt);

			if (!inv.equals(nbtInv)) {
				belt.getOrCreateTag().put("Inventory", inv.createTag());
			}
		}

		stateManager.onStartTick((Player) (Object) this);
	}
}
