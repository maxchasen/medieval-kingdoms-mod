package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.features.faction.KingdomMobLabels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.server.level.ServerLevel;

/** Armorsmith / Weaponsmith-style jobs: pickup, smelt timers, equipment (PRD §3.3–3.4 draft). */
public final class EconomyFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		ModEconomyEntityTypes.register();
		ModEconomyItems.register();

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(world instanceof ServerLevel serverLevel)) {
				return;
			}
			if (entity instanceof ArmorsmithVillagerEntity smith) {
				KingdomMobLabels.refreshForEntity(smith, serverLevel);
			} else if (entity instanceof WeaponsmithVillagerEntity wsmith) {
				KingdomMobLabels.refreshForEntity(wsmith, serverLevel);
			} else if (entity instanceof FletcherVillagerEntity fletcher) {
				KingdomMobLabels.refreshForEntity(fletcher, serverLevel);
			}
		});
	}
}
