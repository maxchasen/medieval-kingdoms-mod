package com.medievalkingdoms.features.village;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.npc.villager.Villager;
import org.slf4j.Logger;

/**
 * Called from mixin when a server-side baby villager finishes {@link Villager#finalizeSpawn}. Uses loaded {@link VillageRoleConfig} weights (future: attach role data / professions).
 */
public final class VillageRoleSpawnHooks {
	private static final Logger LOGGER = LogUtils.getLogger();

	private VillageRoleSpawnHooks() {}

	public static void onBabyVillagerSpawned(Villager villager) {
		var rolled = VillageRoleConfig.get().rollModRole(villager.getRandom());
		if (rolled.isPresent()) {
			LOGGER.debug("[medieval_kingdoms] Baby villager at {} rolled kingdom role: {}", villager.blockPosition(), rolled.get());
		} else {
			LOGGER.debug(
				"[medieval_kingdoms] Baby villager at {} rolled vanilla pathway (weight {}%)",
				villager.blockPosition(),
				VillageRoleConfig.get().vanillaWeight()
			);
		}
	}
}
