package com.medievalkingdoms.features.combat;

import com.medievalkingdoms.MedievalKingdomsMod;
import com.medievalkingdoms.features.combat.entity.ArcherEntity;
import com.medievalkingdoms.features.combat.entity.KnightEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModCombatEntityTypes {
	public static final ResourceKey<EntityType<?>> KNIGHT_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			MedievalKingdomsMod.id("knight"));

	public static final ResourceKey<EntityType<?>> ARCHER_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			MedievalKingdomsMod.id("archer"));

	public static final EntityType<KnightEntity> KNIGHT = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			KNIGHT_KEY,
			EntityType.Builder.of(KnightEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build(KNIGHT_KEY));

	public static final EntityType<ArcherEntity> ARCHER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			ARCHER_KEY,
			EntityType.Builder.of(ArcherEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build(ARCHER_KEY));

	private ModCombatEntityTypes() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(KNIGHT, KnightEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ARCHER, ArcherEntity.createAttributes());
	}
}
