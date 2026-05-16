package com.medievalkingdoms.client;

import com.medievalkingdoms.client.render.ProfessionVillagerRenderer;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/** Registers renderers for combat mobs (Knight / Archer). */
public final class CombatEntityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(
				ModCombatEntityTypes.KNIGHT,
				context -> new ProfessionVillagerRenderer(context, VillagerProfession.WEAPONSMITH));
		EntityRenderers.register(
				ModCombatEntityTypes.ARCHER,
				context -> new ProfessionVillagerRenderer(context, VillagerProfession.FLETCHER));
	}
}
