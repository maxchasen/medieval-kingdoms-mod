package com.medievalkingdoms.features.realm;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Kingdom saved data + kingdom sigil block / block entity (PRD §4.2, §6). Single entrypoint preserves init order.
 */
public final class RealmFeature implements ModInitializer {
	public static final ResourceKey<Block> KINGDOM_SIGIL_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, MedievalKingdomsMod.id("kingdom_sigil"));
	public static final ResourceKey<Item> KINGDOM_SIGIL_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, MedievalKingdomsMod.id("kingdom_sigil"));
	public static final ResourceKey<BlockEntityType<?>> KINGDOM_SIGIL_BE_KEY = ResourceKey.create(
			Registries.BLOCK_ENTITY_TYPE,
			MedievalKingdomsMod.id("kingdom_sigil"));

	public static Block KINGDOM_SIGIL_BLOCK;
	public static Item KINGDOM_SIGIL_ITEM;
	public static BlockEntityType<KingdomSigilBlockEntity> KINGDOM_SIGIL_BE_TYPE;

	@Override
	public void onInitialize() {
		KINGDOM_SIGIL_BLOCK = Registry.register(
				BuiltInRegistries.BLOCK,
				KINGDOM_SIGIL_BLOCK_KEY,
				new KingdomSigilBlock(KingdomSigilBlock.createProperties().setId(KINGDOM_SIGIL_BLOCK_KEY)));
		KINGDOM_SIGIL_ITEM = Registry.register(
				BuiltInRegistries.ITEM,
				KINGDOM_SIGIL_ITEM_KEY,
				new BlockItem(
						KINGDOM_SIGIL_BLOCK,
						new Item.Properties()
								.setId(KINGDOM_SIGIL_ITEM_KEY)
								.useBlockDescriptionPrefix()
								.component(DataComponents.CUSTOM_NAME, Component.translatable("item.medieval_kingdoms.kingdom_sigil"))));
		KINGDOM_SIGIL_BE_TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				KINGDOM_SIGIL_BE_KEY,
				FabricBlockEntityTypeBuilder.create(KingdomSigilBlockEntity::new, KINGDOM_SIGIL_BLOCK).build());

		// Overworld is not guaranteed during SERVER_STARTING; priming must run after dimensions load.
		ServerLifecycleEvents.SERVER_STARTED.register(KingdomWorldData::ensureLoaded);

		ItemGroupEvents.modifyEntriesEvent(
						ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("building_blocks")))
				.register(entries -> entries.addAfter(Items.CHISELED_STONE_BRICKS, KINGDOM_SIGIL_ITEM));
	}
}
