package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.features.economy.ai.PickupIronMaterialsGoal;

import net.minecraft.world.entity.item.ItemEntity;

/** Mobs that use {@link PickupIronMaterialsGoal} ground pickup. */
public interface IronMaterialPickupMob {
	void tryPickupIronItem(ItemEntity itemEntity);
}
