package com.medievalkingdoms.features.faction;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.api.ModInitializer;

/** Villager kingdom NBT, tagging near sigil, and inter-villager targeting hooks (PRD §6.2–6.3). */
public final class FactionFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		MedievalKingdomsMod.LOGGER.debug("Medieval Kingdoms faction hooks active.");
	}
}
