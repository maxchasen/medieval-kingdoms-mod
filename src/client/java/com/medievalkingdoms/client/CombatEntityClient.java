package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.EconomyVillagerRenderer;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Registers renderers for combat mobs (Knight / Archer). */
public final class CombatEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModCombatEntityTypes.KNIGHT, EconomyVillagerRenderer::new);
		EntityRenderers.register(ModCombatEntityTypes.ARCHER, EconomyVillagerRenderer::new);
	}
}
