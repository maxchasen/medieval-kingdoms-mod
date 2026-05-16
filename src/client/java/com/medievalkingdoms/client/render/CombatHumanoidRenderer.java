package com.medievalkingdoms.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Items;

/**
 * Humanoid combat mob renderer with full armor layers and held items.
 * Uses illager skins (vindicator / pillager) on the player model so iron armor renders correctly.
 */
public final class CombatHumanoidRenderer extends HumanoidMobRenderer<PathfinderMob, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private final Identifier texture;
	private final boolean bowArmPose;

	public CombatHumanoidRenderer(EntityRendererProvider.Context context, Identifier texture, boolean bowArmPose) {
		super(
				context,
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
				new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)),
				0.5F);
		this.texture = texture;
		this.bowArmPose = bowArmPose;
		this.addLayer(
				new HumanoidArmorLayer<>(
						this,
						ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
						context.getEquipmentRenderer()));
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return this.texture;
	}

	@Override
	protected HumanoidModel.ArmPose getArmPose(PathfinderMob mob, HumanoidArm arm) {
		if (this.bowArmPose && mob.getMainHandItem().is(Items.BOW)) {
			return HumanoidModel.ArmPose.BOW_AND_ARROW;
		}
		return super.getArmPose(mob, arm);
	}
}
