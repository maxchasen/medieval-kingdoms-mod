package com.medievalkingdoms.features.miner;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.faction.KingdomMobLabels;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Miner excavation / job AI (PRD §3.2). Tooling + labels on spawn.
 */
public final class MinerJobsFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!(entity instanceof MinerEntity miner) || !(world instanceof ServerLevel serverLevel)) {
				return;
			}
			if (miner.getMainHandItem().isEmpty()) {
				ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
				pick.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
				miner.setItemSlot(EquipmentSlot.MAINHAND, pick);
			}
			KingdomMobLabels.applyRoleName(miner, serverLevel, "Miner");
		});
	}
}
