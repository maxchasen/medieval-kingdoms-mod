package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.MinerHumanoidRenderer;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Registers renderers for combat mobs (Knight / Archer). */
public final class CombatEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModCombatEntityTypes.KNIGHT, MinerHumanoidRenderer::new);
	}
}
