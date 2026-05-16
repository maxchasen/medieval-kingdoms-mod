package com.medievalkingdoms.entity;

import com.medievalkingdoms.entity.ai.MinerExcavationGoal;
import com.medievalkingdoms.entity.ai.MinerPickupDropsGoal;
import com.medievalkingdoms.entity.ai.MoveTowardsNearestQuarryGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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

public class MinerEntity extends PathfinderMob {
	public MinerEntity(EntityType<? extends MinerEntity> entityType, Level level) {
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
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MoveTowardsNearestQuarryGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new MinerExcavationGoal(this));
		this.goalSelector.addGoal(4, new MinerPickupDropsGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}

	@Override
	public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack stack) {
		return MinerPickupDropsGoal.isMinerDrop(stack) && super.wantsToPickUp(serverLevel, stack);
	}

	@Override
	protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
		if (!itemEntity.getItem().isEmpty() && this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
			if (this.mergeIntoOffhand(itemEntity)) {
				return;
			}
		}
		super.pickUpItem(serverLevel, itemEntity);
	}

	public void tryPickupGroundDrop(ItemEntity itemEntity) {
		if (this.level() instanceof ServerLevel serverLevel
				&& !itemEntity.hasPickUpDelay()
				&& this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
			this.pickUpItem(serverLevel, itemEntity);
		}
	}

	public boolean isNearQuarryWorkSite() {
		return MinerPickupDropsGoal.isNearQuarryWorkSite(this.level(), this.position());
	}

	private boolean mergeIntoOffhand(ItemEntity itemEntity) {
		ItemStack incoming = itemEntity.getItem();
		if (incoming.isEmpty()) {
			return false;
		}
		ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
		if (offhand.isEmpty()) {
			this.setItemSlot(EquipmentSlot.OFFHAND, incoming.split(incoming.getCount()));
			if (incoming.isEmpty()) {
				itemEntity.discard();
			} else {
				itemEntity.setItem(incoming);
			}
			return incoming.isEmpty();
		}
		if (!ItemStack.isSameItemSameComponents(offhand, incoming)) {
			return false;
		}
		int space = offhand.getMaxStackSize() - offhand.getCount();
		if (space <= 0) {
			return false;
		}
		int take = Math.min(space, incoming.getCount());
		offhand.grow(take);
		incoming.shrink(take);
		this.setItemSlot(EquipmentSlot.OFFHAND, offhand);
		itemEntity.setItem(incoming);
		if (incoming.isEmpty()) {
			itemEntity.discard();
		}
		return incoming.isEmpty();
	}
}
