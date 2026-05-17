package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.features.faction.KingdomMobLabels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;

/** Economy jobs on vanilla villager professions (PRD §3.3–3.4). */
public final class EconomyFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(world instanceof ServerLevel serverLevel)) {
				return;
			}
			if (entity instanceof Villager villager) {
				VillagerProfessionGoals.ensureRegistered(villager);
				KingdomMobLabels.refreshForEntity(villager, serverLevel);
			}
		});
	}
}
