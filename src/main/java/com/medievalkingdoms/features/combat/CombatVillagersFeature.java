package com.medievalkingdoms.features.combat;

import net.fabricmc.api.ModInitializer;

/**
 * Combat villager roles: Knight / Archer skeleton (PRD §3.7–3.8, §8). Expand in later milestones.
 */
public final class CombatVillagersFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		ModCombatEntityTypes.register();
		ModCombatItems.register();
	}
}
