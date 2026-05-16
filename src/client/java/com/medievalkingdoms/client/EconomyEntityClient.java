package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.MinerHumanoidRenderer;
import com.medievalkingdoms.features.economy.ModEconomyEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Client registration for economy mobs (armorsmith, etc.). */
public final class EconomyEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEconomyEntityTypes.ARMORSMITH_VILLAGER, MinerHumanoidRenderer::new);
	}
}
