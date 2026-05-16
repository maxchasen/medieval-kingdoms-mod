package com.medievalkingdoms.client.render;

import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Renders economy-role mobs with the villager model/skin instead of the shared zombie placeholder.
 */
public final class EconomyVillagerRenderer extends MobRenderer<PathfinderMob, VillagerRenderState, VillagerModel> {
	private static final Identifier VILLAGER_SKIN =
			Identifier.withDefaultNamespace("textures/entity/villager/villager.png");

	public EconomyVillagerRenderer(EntityRendererProvider.Context context) {
		super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
	}

	@Override
	public VillagerRenderState createRenderState() {
		return new VillagerRenderState();
	}

	@Override
	public void extractRenderState(PathfinderMob entity, VillagerRenderState state, float tickCount) {
		super.extractRenderState(entity, state, tickCount);
	}

	@Override
	public Identifier getTextureLocation(VillagerRenderState state) {
		return VILLAGER_SKIN;
	}
}
