package com.medievalkingdoms.features.miner;

import net.fabricmc.api.ModInitializer;

/**
 * Miner excavation / job AI (PRD §3.2). Implemented by async agent; entrypoint loads the module.
 */
public final class MinerJobsFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		// Miner excavation uses datapack tag medieval_kingdoms:miner_mineable and AI goals only.
	}
}
