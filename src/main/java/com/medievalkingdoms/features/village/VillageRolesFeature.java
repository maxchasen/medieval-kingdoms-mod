package com.medievalkingdoms.features.village;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Village role weights config + hooks into villager lifecycle (PRD §3, §9 #13).
 */
public final class VillageRolesFeature implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String CONFIG_SUBDIR = "medieval_kingdoms";
	private static final String CONFIG_FILE = "village_roles.json";

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_SUBDIR).resolve(CONFIG_FILE);
			try {
				VillageRoleConfig.reload(path);
				LOGGER.info("Loaded village role weights from {}", path);
			} catch (IOException e) {
				LOGGER.error("Failed to load or create {}", path, e);
			}
		});
	}
}
