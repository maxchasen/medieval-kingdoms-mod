package com.medievalkingdoms.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.medievalkingdoms.features.faction.VillagerKingdom;
import com.medievalkingdoms.features.economy.VillagerProfessionGoals;
import com.medievalkingdoms.features.village.VillageQuarryPlacer;
import com.medievalkingdoms.features.village.VillageRoleEnsurer;
import com.medievalkingdoms.features.village.VillageRoleSpawnHooks;

@Mixin(Villager.class)
public abstract class VillagerKingdomScanMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	private void medievalKingdoms$tagNearSigil(CallbackInfo ci) {
		Villager villager = (Villager) (Object) this;
		if (villager.level().isClientSide()) {
			return;
		}
		if (!(villager.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (villager.tickCount % 40 != 0) {
			return;
		}
		VillageRoleSpawnHooks.tryApplyPendingRole(villager);
		VillagerProfessionGoals.ensureRegistered(villager);
		if (villager.tickCount % 200 == Math.floorMod(villager.getId(), 200)) {
			VillageRoleEnsurer.tryBackfillMissingRoles(serverLevel, villager);
			VillageQuarryPlacer.tryPlaceNearVillage(serverLevel, villager);
		}
		VillagerKingdom.tryTagFromNearbySigil(serverLevel, villager);
	}
}
