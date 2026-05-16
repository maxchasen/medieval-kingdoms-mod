package com.medievalkingdoms.features.combat;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModCombatItems {
	public static final ResourceKey<Item> KNIGHT_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			MedievalKingdomsMod.id("knight_spawn_egg"));

	public static final Item KNIGHT_SPAWN_EGG = Registry.register(
			BuiltInRegistries.ITEM,
			KNIGHT_SPAWN_EGG_KEY,
			new SpawnEggItem(
					new Item.Properties().setId(KNIGHT_SPAWN_EGG_KEY).spawnEgg(ModCombatEntityTypes.KNIGHT)));

	private ModCombatItems() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(KNIGHT_SPAWN_EGG));
	}
}
