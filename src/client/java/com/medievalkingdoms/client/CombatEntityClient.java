package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.CombatHumanoidRenderer;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.Identifier;

/** Registers renderers for combat mobs (Knight / Archer). */
public final class CombatEntityClient implements ClientModInitializer {
	private static final Identifier KNIGHT_TEXTURE =
			Identifier.withDefaultNamespace("textures/entity/illager/vindicator.png");
	private static final Identifier ARCHER_TEXTURE =
			Identifier.withDefaultNamespace("textures/entity/illager/pillager.png");

	@Override
	public void onInitializeClient() {
		EntityRenderers.register(
				ModCombatEntityTypes.KNIGHT, context -> new CombatHumanoidRenderer(context, KNIGHT_TEXTURE, false));
		EntityRenderers.register(
				ModCombatEntityTypes.ARCHER, context -> new CombatHumanoidRenderer(context, ARCHER_TEXTURE, true));
	}
}
