package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.features.economy.ai.PickupIronMaterialsGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Economy villager role: collects iron inputs plus ingots for smithing weapons.
 */
public final class WeaponsmithVillagerEntity extends PathfinderMob implements IronMaterialPickupMob {
	public WeaponsmithVillagerEntity(EntityType<? extends WeaponsmithVillagerEntity> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.FOLLOW_RANGE, 64.0D);
	}

	@Override
	public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack stack) {
		return PickupIronMaterialsGoal.stackIsWeaponsmithPickup(stack) && super.wantsToPickUp(serverLevel, stack);
	}

	@Override
	public void tryPickupIronItem(ItemEntity itemEntity) {
		if (this.level() instanceof ServerLevel serverLevel
				&& !itemEntity.hasPickUpDelay()
				&& this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
			this.pickUpItem(serverLevel, itemEntity);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new PickupIronMaterialsGoal(this, 1.0D, PickupIronMaterialsGoal::stackIsWeaponsmithPickup));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}
}
