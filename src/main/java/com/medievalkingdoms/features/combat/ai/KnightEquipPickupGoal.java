package com.medievalkingdoms.features.combat.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import com.medievalkingdoms.features.combat.entity.KnightEntity;

import org.jspecify.annotations.Nullable;

/** Picks up iron swords and any equippable helmet for knights with empty slots. */
public final class KnightEquipPickupGoal extends Goal {
	private static final double RANGE = 10.0D;
	private static final double RANGE_SQ = RANGE * RANGE;
	private static final double PICKUP_SQ = 2.25D;

	private final KnightEntity knight;
	private final double speedModifier;
	@Nullable
	private ItemEntity groundItem;

	public KnightEquipPickupGoal(KnightEntity knight, double speedModifier) {
		this.knight = knight;
		this.speedModifier = speedModifier;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		Level level = this.knight.level();
		if (level.isClientSide()) {
			return false;
		}
		this.groundItem = findGroundLoot(stack -> isKnightEquipLoot(stack) && canPickUpNow(stack));
		return this.groundItem != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.groundItem != null
				&& this.groundItem.isAlive()
				&& !this.groundItem.getItem().isEmpty()
				&& !this.groundItem.hasPickUpDelay()
				&& this.knight.distanceToSqr(this.groundItem) <= RANGE_SQ + 4.0D;
	}

	@Override
	public void start() {
		if (this.groundItem != null) {
			this.knight.getNavigation().moveTo(this.groundItem, this.speedModifier);
		}
	}

	@Override
	public void stop() {
		this.groundItem = null;
		this.knight.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.groundItem == null) {
			return;
		}
		ItemStack stack = this.groundItem.getItem();
		if (stack.isEmpty() || this.groundItem.hasPickUpDelay()) {
			return;
		}
		if (!isKnightEquipLoot(stack)) {
			return;
		}
		if (this.knight.distanceToSqr(this.groundItem) < PICKUP_SQ) {
			tryEquipFromGround(stack);
			if (stack.isEmpty()) {
				this.groundItem.discard();
			}
			this.stop();
			return;
		}
		if (this.knight.getNavigation().isDone()) {
			this.knight.getNavigation().moveTo(this.groundItem, this.speedModifier);
		}
	}

	private void tryEquipFromGround(ItemStack stack) {
		if (isSword(stack) && this.knight.getMainHandItem().isEmpty()) {
			this.knight.setItemSlot(EquipmentSlot.MAINHAND, stack.split(stack.getCount()));
			return;
		}
		Equippable equip = stack.get(DataComponents.EQUIPPABLE);
		if (equip != null && equip.slot() == EquipmentSlot.HEAD && this.knight.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			this.knight.setItemSlot(EquipmentSlot.HEAD, stack.split(1));
		}
	}

	private boolean canPickUpNow(ItemStack stack) {
		if (isSword(stack)) {
			return this.knight.getMainHandItem().isEmpty();
		}
		if (isHelmet(stack)) {
			return this.knight.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
		}
		return false;
	}

	private static boolean isKnightEquipLoot(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		return isSword(stack) || isHelmet(stack);
	}

	private static boolean isSword(ItemStack stack) {
		return stack.is(net.minecraft.world.item.Items.IRON_SWORD);
	}

	private static boolean isHelmet(ItemStack stack) {
		Equippable equip = stack.get(DataComponents.EQUIPPABLE);
		return equip != null && equip.slot() == EquipmentSlot.HEAD;
	}

	@Nullable
	private ItemEntity findGroundLoot(Predicate<ItemStack> predicate) {
		AABB search = this.knight.getBoundingBox().inflate(RANGE);
		List<ItemEntity> items = this.knight.level().getEntitiesOfClass(ItemEntity.class, search, e -> {
			if (!e.isAlive() || e.hasPickUpDelay()) {
				return false;
			}
			return predicate.test(e.getItem());
		});
		ItemEntity best = null;
		double bestD = Double.MAX_VALUE;
		for (ItemEntity e : items) {
			double d = this.knight.distanceToSqr(e);
			if (d < bestD && d <= RANGE_SQ) {
				bestD = d;
				best = e;
			}
		}
		return best;
	}
}
