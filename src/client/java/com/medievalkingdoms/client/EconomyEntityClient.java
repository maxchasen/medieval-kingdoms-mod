package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.MinerHumanoidRenderer;
import com.medievalkingdoms.features.economy.ModEconomyEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Client registration for economy mobs (armorsmith, weaponsmith, fletcher). */
public final class EconomyEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEconomyEntityTypes.ARMORSMITH_VILLAGER, MinerHumanoidRenderer::new);
		EntityRenderers.register(ModEconomyEntityTypes.WEAPONSMITH_VILLAGER, MinerHumanoidRenderer::new);
		EntityRenderers.register(ModEconomyEntityTypes.FLETCHER_VILLAGER, MinerHumanoidRenderer::new);
	}
}
