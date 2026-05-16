package com.medievalkingdoms.registry;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final ResourceKey<Item> MINER_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			MedievalKingdomsMod.id("miner_spawn_egg"));

	public static final Item MINER_SPAWN_EGG = Registry.register(
			BuiltInRegistries.ITEM,
			MINER_SPAWN_EGG_KEY,
			new SpawnEggItem(
					new Item.Properties().setId(MINER_SPAWN_EGG_KEY).spawnEgg(ModEntityTypes.MINER)));

	private ModItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(MINER_SPAWN_EGG));
	}
}
