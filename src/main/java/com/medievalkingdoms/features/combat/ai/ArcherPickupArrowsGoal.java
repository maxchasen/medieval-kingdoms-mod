package com.medievalkingdoms.features.combat.ai;

import com.medievalkingdoms.features.combat.entity.ArcherEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/** Paths toward arrow drops and stores them in the offhand for shooting. */
public final class ArcherPickupArrowsGoal extends Goal {
	private static final double SEARCH_RANGE = 10.0D;
	private static final double PICKUP_SQ = 2.25D;

	private final ArcherEntity archer;
	private final double speedModifier;
	private ItemEntity targetItem;

	public ArcherPickupArrowsGoal(ArcherEntity archer, double speedModifier) {
		this.archer = archer;
		this.speedModifier = speedModifier;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.archer.level().isClientSide()) {
			return false;
		}
		this.targetItem = findNearestArrow();
		return this.targetItem != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetItem != null
				&& this.targetItem.isAlive()
				&& !this.targetItem.getItem().isEmpty()
				&& this.archer.distanceToSqr(this.targetItem) <= (SEARCH_RANGE + 1.0D) * (SEARCH_RANGE + 1.0D);
	}

	@Override
	public void start() {
		if (this.targetItem != null) {
			this.archer.getNavigation().moveTo(this.targetItem, this.speedModifier);
		}
	}

	@Override
	public void stop() {
		this.targetItem = null;
	}

	@Override
	public void tick() {
		if (this.targetItem == null || !this.targetItem.isAlive() || this.targetItem.getItem().isEmpty()) {
			return;
		}
		if (this.archer.distanceToSqr(this.targetItem) < PICKUP_SQ) {
			this.tryMergeArrow(this.targetItem);
			return;
		}
		if (this.archer.getNavigation().isDone()) {
			this.archer.getNavigation().moveTo(this.targetItem, this.speedModifier);
		}
	}

	private void tryMergeArrow(ItemEntity itemEntity) {
		if (!(this.archer.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		ItemStack incoming = itemEntity.getItem();
		if (!incoming.is(Items.ARROW)) {
			return;
		}
		ItemStack offhand = this.archer.getItemBySlot(EquipmentSlot.OFFHAND);
		if (offhand.isEmpty()) {
			this.archer.setItemSlot(EquipmentSlot.OFFHAND, incoming.split(incoming.getCount()));
			if (incoming.isEmpty()) {
				itemEntity.discard();
			} else {
				itemEntity.setItem(incoming);
			}
			return;
		}
		if (ItemStack.isSameItemSameComponents(offhand, incoming)) {
			int space = offhand.getMaxStackSize() - offhand.getCount();
			if (space > 0) {
				int moved = Math.min(space, incoming.getCount());
				offhand.grow(moved);
				incoming.shrink(moved);
				this.archer.setItemSlot(EquipmentSlot.OFFHAND, offhand);
				itemEntity.setItem(incoming);
				if (incoming.isEmpty()) {
					itemEntity.discard();
				}
				return;
			}
		}
	}

	private ItemEntity findNearestArrow() {
		AABB search = this.archer.getBoundingBox().inflate(SEARCH_RANGE);
		List<ItemEntity> items = this.archer.level().getEntitiesOfClass(ItemEntity.class, search, e -> {
			return e.isAlive() && !e.hasPickUpDelay() && e.getItem().is(Items.ARROW);
		});
		ItemEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (ItemEntity entity : items) {
			double d = this.archer.distanceToSqr(entity);
			if (d < best) {
				best = d;
				nearest = entity;
			}
		}
		return nearest;
	}
}
