package com.medievalkingdoms.block;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModBlocks {
	private ModBlocks() {
	}

	public static final Block QUARRY = registerBlock("quarry", new QuarryBlock());

	private static Block registerBlock(String name, Block block) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MedievalKingdomsMod.MOD_ID, name);
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
		return block;
	}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> entries.accept(QUARRY));
	}
}
