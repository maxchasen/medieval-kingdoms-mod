package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.EconomyVillagerRenderer;
import com.medievalkingdoms.features.economy.ModEconomyEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Client registration for economy mobs (armorsmith, weaponsmith, fletcher). */
public final class EconomyEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEconomyEntityTypes.ARMORSMITH_VILLAGER, EconomyVillagerRenderer::new);
		EntityRenderers.register(ModEconomyEntityTypes.WEAPONSMITH_VILLAGER, EconomyVillagerRenderer::new);
		EntityRenderers.register(ModEconomyEntityTypes.FLETCHER_VILLAGER, EconomyVillagerRenderer::new);
	}
}
