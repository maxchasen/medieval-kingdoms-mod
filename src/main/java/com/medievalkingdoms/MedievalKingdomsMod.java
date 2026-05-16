package com.medievalkingdoms;

import com.medievalkingdoms.registry.ModBlocks;
import com.medievalkingdoms.registry.ModEntityTypes;
import com.medievalkingdoms.registry.ModItems;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedievalKingdomsMod implements ModInitializer {
	public static final String MOD_ID = "medieval_kingdoms";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModEntityTypes.register();
		ModItems.register();
		LOGGER.info("Medieval Kingdoms (Fabric) initialized.");
	}
}
