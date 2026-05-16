package com.medievalkingdoms.client.render;

import com.medievalkingdoms.entity.MinerEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

public final class MinerHumanoidRenderer extends HumanoidMobRenderer<MinerEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");

	public MinerHumanoidRenderer(EntityRendererProvider.Context context) {
		super(
				context,
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
				0.5F);
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState humanoidRenderState) {
		return TEXTURE;
	}
}
