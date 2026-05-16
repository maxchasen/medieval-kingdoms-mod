package com.medievalkingdoms.features.village;

import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.mojang.logging.LogUtils;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.slf4j.Logger;

/**
 * Rolls mod roles for villagers on spawn; babies keep {@link MedievalKingdomsMobTags#VILLAGE_ROLE_ID} until adult,
 * adults convert immediately when the roll maps to a mod entity type. Missing roles in the village are filled first.
 */
public final class VillageRoleSpawnHooks {
	private static final Logger LOGGER = LogUtils.getLogger();

	private VillageRoleSpawnHooks() {}

	public static void onVillagerSpawned(Villager villager) {
		if (!(villager.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		Optional<String> rolled = VillageRoleEnsurer.pickRoleForSpawn(serverLevel, villager);
		if (rolled.isEmpty()) {
			return;
		}
		String roleId = rolled.get();
		if (villager.isBaby()) {
			MedievalKingdomsMobTags.writeVillageRole(villager, roleId);
			LOGGER.debug("[medieval_kingdoms] Baby villager at {} assigned pending role '{}'", villager.blockPosition(), roleId);
			return;
		}
		if (VillageRoleApplicator.replaceWithRole(villager, roleId)) {
			LOGGER.info("[medieval_kingdoms] Villager at {} became role '{}'", villager.blockPosition(), roleId);
		}
	}

	public static void tryApplyPendingRole(Villager villager) {
		if (VillageRoleApplicator.tryApplyStoredRole(villager)) {
			LOGGER.info("[medieval_kingdoms] Baby villager grew into role at {}", villager.blockPosition());
		}
	}
}
