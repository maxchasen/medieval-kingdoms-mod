package com.medievalkingdoms.features.economy;

import net.fabricmc.api.ModInitializer;

/** Armorsmith / Weaponsmith-style jobs: pickup, smelt timers, equipment (PRD §3.3–3.4 draft). */
public final class EconomyFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		ModEconomyEntityTypes.register();
		ModEconomyItems.register();
	}
}
