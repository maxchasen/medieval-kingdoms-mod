package com.medievalkingdoms.mixin;

import com.medievalkingdoms.features.economy.ai.FletcherSupplyArcherGoal;
import com.medievalkingdoms.features.economy.ai.PickupIronMaterialsGoal;
import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerProfessionGoalsMixin {
	@Shadow
	protected GoalSelector goalSelector;

	@Inject(method = "registerGoals", at = @At("RETURN"))
	private void medievalKingdoms$onRegisterGoals(CallbackInfo ci) {
		addEconomyGoals();
	}

	public void addEconomyGoals() {
		Villager self = (Villager) (Object) this;
		if (MedievalKingdomsMobTags.hasEconomyGoalsRegistered(self) || self.isBaby()) {
			return;
		}
		var profession = self.getVillagerData().profession();
		if (profession.is(VillagerProfession.WEAPONSMITH)) {
			self.setCanPickUpLoot(true);
			this.goalSelector.addGoal(
					2, new PickupIronMaterialsGoal(self, 1.0D, PickupIronMaterialsGoal::stackIsWeaponsmithPickup));
		} else if (profession.is(VillagerProfession.ARMORER)) {
			self.setCanPickUpLoot(true);
			this.goalSelector.addGoal(
					2, new PickupIronMaterialsGoal(self, 1.0D, PickupIronMaterialsGoal::stackIsArmorsmithPickup));
		} else if (profession.is(VillagerProfession.FLETCHER)) {
			this.goalSelector.addGoal(3, new FletcherSupplyArcherGoal(self, 1.0D));
		}
		MedievalKingdomsMobTags.markEconomyGoalsRegistered(self);
	}
}
