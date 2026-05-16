package com.medievalkingdoms.registry;

import com.medievalkingdoms.MedievalKingdomsMod;
import com.medievalkingdoms.entity.MinerEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
	public static final ResourceKey<EntityType<?>> MINER_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			MedievalKingdomsMod.id("miner"));

	public static final EntityType<MinerEntity> MINER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			MINER_KEY,
			EntityType.Builder.of(MinerEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build(MINER_KEY));

	private ModEntityTypes() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(MINER, MinerEntity.createAttributes());
	}
}
