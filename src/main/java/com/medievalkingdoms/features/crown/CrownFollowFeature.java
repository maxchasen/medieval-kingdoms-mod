package com.medievalkingdoms.features.crown;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Knight follow behavior when kingdom owner wears the Warlord crown (PRD §5.1). */
public final class CrownFollowFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		// No per-tick world scan: following is driven by {@link CrownFollowGoal} on {@link com.medievalkingdoms.features.combat.entity.KnightEntity}.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
		});
	}
}
