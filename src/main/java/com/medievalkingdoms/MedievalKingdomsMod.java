package com.medievalkingdoms;

import com.medievalkingdoms.block.ModBlocks;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedievalKingdomsMod implements ModInitializer {
	public static final String MOD_ID = "medieval_kingdoms";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.register();
		LOGGER.info("Medieval Kingdoms (Fabric) initialized.");
	}
}
