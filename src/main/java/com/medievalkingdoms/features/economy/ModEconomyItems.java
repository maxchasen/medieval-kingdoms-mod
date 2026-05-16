package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
					new Item.Properties().setId(ARMORSMITH_SPAWN_EGG_KEY).spawnEgg(ModEconomyEntityTypes.ARMORSMITH_VILLAGER)));

	private ModEconomyItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(ARMORSMITH_SPAWN_EGG));
	}
}
