package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.EconomyVillagerRenderer;
import com.medievalkingdoms.registry.ModEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class MedievalKingdomsModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntityTypes.MINER, EconomyVillagerRenderer::new);
	}
}
