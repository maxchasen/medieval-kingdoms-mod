package com.medievalkingdoms.features.crown;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Warlord's Crown item — helmet slot, copper-tier armor, unbreakable (PRD §5.1).
 */
public final class CrownFeature implements ModInitializer {
	public static final ResourceKey<Item> WARLORDS_CROWN_KEY = ResourceKey.create(Registries.ITEM, MedievalKingdomsMod.id("crown"));

	public static final Item WARLORDS_CROWN = Registry.register(
			BuiltInRegistries.ITEM,
			WARLORDS_CROWN_KEY,
			new Item(
					new Item.Properties()
							.setId(WARLORDS_CROWN_KEY)
							.humanoidArmor(ArmorMaterials.COPPER, ArmorType.HELMET)
							.component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> entries.accept(WARLORDS_CROWN));
		// TODO: crown follow goal — path-follow behavior for allied knights near the crowned player (PRD §5.1).
	}
}
