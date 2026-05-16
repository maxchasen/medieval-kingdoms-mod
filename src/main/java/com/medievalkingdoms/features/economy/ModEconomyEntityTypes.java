package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEconomyEntityTypes {
	public static final ResourceKey<EntityType<?>> ARMORSMITH_VILLAGER_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			MedievalKingdomsMod.id("armorsmith_villager"));

	public static final EntityType<ArmorsmithVillagerEntity> ARMORSMITH_VILLAGER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			ARMORSMITH_VILLAGER_KEY,
			EntityType.Builder.of(ArmorsmithVillagerEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.clientTrackingRange(10)
					.build(ARMORSMITH_VILLAGER_KEY));

	private ModEconomyEntityTypes() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(ARMORSMITH_VILLAGER, ArmorsmithVillagerEntity.createAttributes());
	}
}
