package com.medievalkingdoms.client.render;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

public final class ProfessionVillagerRenderer extends EconomyVillagerRenderer {
	private final Holder<VillagerProfession> profession;

	public ProfessionVillagerRenderer(EntityRendererProvider.Context context, ResourceKey<VillagerProfession> profession) {
		super(context);
		this.profession = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(profession);
		this.addLayer(
				new VillagerProfessionLayer<>(
						this,
						context.getResourceManager(),
						"villager",
						new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_NO_HAT)),
						new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY_NO_HAT))));
	}

	@Override
	public void extractRenderState(PathfinderMob entity, VillagerRenderState state, float tickCount) {
		super.extractRenderState(entity, state, tickCount);
		Holder<VillagerType> type = BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS);
		state.villagerData = new VillagerData(type, this.profession, 1);
	}
}
