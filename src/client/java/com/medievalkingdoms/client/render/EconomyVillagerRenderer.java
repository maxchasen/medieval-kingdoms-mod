package com.medievalkingdoms.client.render;

import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders combat / job mobs with the villager model, including held items and headwear
 * (vanilla villagers use crossed-arms item + custom head layers instead of humanoid armor).
 */
public class EconomyVillagerRenderer extends MobRenderer<PathfinderMob, VillagerRenderState, VillagerModel> {
	private static final Identifier VILLAGER_SKIN =
			Identifier.withDefaultNamespace("textures/entity/villager/villager.png");

	private final Identifier texture;

	public EconomyVillagerRenderer(EntityRendererProvider.Context context) {
		this(context, VILLAGER_SKIN);
	}

	public EconomyVillagerRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
		this.texture = texture;
		this.addLayer(
				new CustomHeadLayer<>(
						this, context.getModelSet(), context.getPlayerSkinRenderCache(), VillagerRenderer.CUSTOM_HEAD_TRANSFORMS));
		this.addLayer(new CrossedArmsItemLayer<>(this));
	}

	@Override
	public VillagerRenderState createRenderState() {
		return new VillagerRenderState();
	}

	@Override
	public void extractRenderState(PathfinderMob entity, VillagerRenderState state, float tickCount) {
		super.extractRenderState(entity, state, tickCount);
		HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, this.itemModelResolver);
		ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
		if (!head.isEmpty() && HumanoidArmorLayer.shouldRender(head, EquipmentSlot.HEAD)) {
			this.itemModelResolver.updateForLiving(state.headItem, head, ItemDisplayContext.HEAD, entity);
		}
		state.isUnhappy = false;
		state.villagerData = null;
	}

	@Override
	public Identifier getTextureLocation(VillagerRenderState state) {
		return this.texture;
	}
}
