package com.medievalkingdoms.features.village;

import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.npc.villager.Villager;
import org.slf4j.Logger;

/**
 * Called from mixin when a server-side baby villager finishes {@link Villager#finalizeSpawn}. Uses loaded {@link VillageRoleConfig} weights;
 * non-empty rolls persist {@link MedievalKingdomsMobTags#VILLAGE_ROLE_ID} on the entity {@code data} component.
 */
public final class VillageRoleSpawnHooks {
	private static final Logger LOGGER = LogUtils.getLogger();

	private VillageRoleSpawnHooks() {}

	public static void onBabyVillagerSpawned(Villager villager) {
		var rolled = VillageRoleConfig.get().rollModRole(villager.getRandom());
		if (rolled.isPresent()) {
			String roleId = rolled.get();
			MedievalKingdomsMobTags.writeVillageRole(villager, roleId);
			LOGGER.info("[medieval_kingdoms] Assigned village role '{}' to baby villager at {} (uuid {})", roleId, villager.blockPosition(), villager.getUUID());
		} else {
			LOGGER.debug(
				"[medieval_kingdoms] Baby villager at {} rolled vanilla pathway (weight {}%)",
				villager.blockPosition(),
				VillageRoleConfig.get().vanillaWeight()
			);
		}
	}
}
