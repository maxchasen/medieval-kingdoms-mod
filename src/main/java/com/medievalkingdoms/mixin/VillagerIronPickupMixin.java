package com.medievalkingdoms.mixin;

import com.medievalkingdoms.features.economy.ai.PickupIronMaterialsGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerIronPickupMixin {
	@Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
	private void medievalKingdoms$smithPickup(ServerLevel level, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		Villager self = (Villager) (Object) this;
		var profession = self.getVillagerData().profession();
		if (profession.is(VillagerProfession.WEAPONSMITH) && PickupIronMaterialsGoal.stackIsWeaponsmithPickup(stack)) {
			cir.setReturnValue(true);
		} else if (profession.is(VillagerProfession.ARMORER) && PickupIronMaterialsGoal.stackIsArmorsmithPickup(stack)) {
			cir.setReturnValue(true);
		}
	}
}
