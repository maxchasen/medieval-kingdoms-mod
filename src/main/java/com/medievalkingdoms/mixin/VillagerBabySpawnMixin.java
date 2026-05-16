package com.medievalkingdoms.mixin;

import com.medievalkingdoms.features.village.VillageRoleSpawnHooks;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Kingdom role weight hook: server baby villagers after {@link Villager#finalizeSpawn} completes (covers breeding and other baby spawns).
 */
@Mixin(Villager.class)
public class VillagerBabySpawnMixin {
	@Inject(method = "finalizeSpawn", at = @At("RETURN"))
	private void medieval_kingdoms$afterFinalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		EntitySpawnReason entitySpawnReason,
		@Nullable SpawnGroupData spawnGroupData,
		CallbackInfoReturnable<@Nullable SpawnGroupData> cir
	) {
		Villager self = (Villager) (Object) this;
		if (self.level().isClientSide() || !self.isBaby()) {
			return;
		}
		VillageRoleSpawnHooks.onBabyVillagerSpawned(self);
	}
}
