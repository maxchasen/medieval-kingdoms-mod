package com.medievalkingdoms.registry;

import com.medievalkingdoms.MedievalKingdomsMod;
import com.medievalkingdoms.block.QuarryBlock;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class ModBlocks {
	public static final ResourceKey<Block> QUARRY_BLOCK_KEY = ResourceKey.create(Registries.BLOCK, MedievalKingdomsMod.id("quarry"));
	public static final ResourceKey<Item> QUARRY_ITEM_KEY = ResourceKey.create(Registries.ITEM, MedievalKingdomsMod.id("quarry"));

	public static final Block QUARRY_BLOCK = registerBlock(QUARRY_BLOCK_KEY, new QuarryBlock(QuarryBlock.createProperties()));
	public static final Item QUARRY_ITEM = registerItem(QUARRY_ITEM_KEY, new BlockItem(QUARRY_BLOCK, new Item.Properties()));

	private ModBlocks() {
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(
				ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("building_blocks")))
			.register(entries -> entries.addAfter(Items.CHISELED_STONE_BRICKS, QUARRY_ITEM));
	}

	private static Block registerBlock(ResourceKey<Block> key, Block block) {
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	private static Item registerItem(ResourceKey<Item> key, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
