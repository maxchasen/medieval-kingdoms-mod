package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModEconomyItems {
	public static final ResourceKey<Item> ARMORSMITH_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			MedievalKingdomsMod.id("armorsmith_spawn_egg"));

	public static final Item ARMORSMITH_SPAWN_EGG = Registry.register(
			BuiltInRegistries.ITEM,
			ARMORSMITH_SPAWN_EGG_KEY,
			new SpawnEggItem(
					new Item.Properties()
							.setId(ARMORSMITH_SPAWN_EGG_KEY)
							.spawnEgg(ModEconomyEntityTypes.ARMORSMITH_VILLAGER)
							.component(DataComponents.CUSTOM_NAME, Component.translatable("item.medieval_kingdoms.armorsmith_spawn_egg"))));

	public static final ResourceKey<Item> WEAPONSMITH_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			MedievalKingdomsMod.id("weaponsmith_spawn_egg"));

	public static final Item WEAPONSMITH_SPAWN_EGG = Registry.register(
			BuiltInRegistries.ITEM,
			WEAPONSMITH_SPAWN_EGG_KEY,
			new SpawnEggItem(
					new Item.Properties()
							.setId(WEAPONSMITH_SPAWN_EGG_KEY)
							.spawnEgg(ModEconomyEntityTypes.WEAPONSMITH_VILLAGER)
							.component(DataComponents.CUSTOM_NAME, Component.translatable("item.medieval_kingdoms.weaponsmith_spawn_egg"))));

	public static final ResourceKey<Item> FLETCHER_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			MedievalKingdomsMod.id("fletcher_spawn_egg"));

	public static final Item FLETCHER_SPAWN_EGG = Registry.register(
			BuiltInRegistries.ITEM,
			FLETCHER_SPAWN_EGG_KEY,
			new SpawnEggItem(
					new Item.Properties()
							.setId(FLETCHER_SPAWN_EGG_KEY)
							.spawnEgg(ModEconomyEntityTypes.FLETCHER_VILLAGER)
							.component(DataComponents.CUSTOM_NAME, Component.translatable("item.medieval_kingdoms.fletcher_spawn_egg"))));

	private ModEconomyItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(ARMORSMITH_SPAWN_EGG);
			entries.accept(WEAPONSMITH_SPAWN_EGG);
			entries.accept(FLETCHER_SPAWN_EGG);
		});
	}
}
