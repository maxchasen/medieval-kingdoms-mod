package com.medievalkingdoms;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedievalKingdomsMod implements ModInitializer {
	public static final String MOD_ID = "medieval_kingdoms";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Medieval Kingdoms (Fabric) initialized.");
	}
}
