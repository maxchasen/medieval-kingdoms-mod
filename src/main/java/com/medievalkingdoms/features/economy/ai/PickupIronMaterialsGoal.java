package com.medievalkingdoms.features.economy.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import com.medievalkingdoms.features.economy.IronMaterialPickupMob;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Paths toward iron-related item drops within range. Vanilla 1.21.11 has {@link net.minecraft.world.entity.ai.goal.UseItemGoal}
 * for using items in hand, not ground pickup; there is no {@code PickUpItemGoal}, so this {@link Goal} paths to {@link ItemEntity} instances.
 */
public final class PickupIronMaterialsGoal extends Goal {
	public static final double ITEM_SEARCH_RANGE = 8.0D;
	private static final double TRY_PICKUP_DISTANCE_SQ = 2.25D; // ~1.5 blocks

	private final PathfinderMob mob;
	private final double speedModifier;
	private final Predicate<ItemStack> pickupPredicate;
	@Nullable
	private ItemEntity targetItem;

	public PickupIronMaterialsGoal(PathfinderMob pathfinderMob, double speedModifier) {
		this(pathfinderMob, speedModifier, PickupIronMaterialsGoal::stackIsArmorsmithPickup);
	}

	public PickupIronMaterialsGoal(
			PathfinderMob pathfinderMob, double speedModifier, Predicate<ItemStack> pickupPredicate) {
		this.mob = pathfinderMob;
		this.speedModifier = speedModifier;
		this.pickupPredicate = pickupPredicate;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	public static boolean stackIsArmorsmithPickup(ItemStack stack) {
		// Include ingots so behaviour matches player expectations for "smith" gatherers.
		return !stack.isEmpty()
				&& (stack.is(Items.RAW_IRON) || stack.is(Items.IRON_INGOT) || stack.is(Items.IRON_ORE) || stack.is(ItemTags.IRON_ORES));
	}

	public static boolean stackIsWeaponsmithPickup(ItemStack stack) {
		return stackIsArmorsmithPickup(stack);
	}

	/**
	 * Fills partial main/offhand stacks before vanilla pickup tries to replace the main hand.
	 */
	public static boolean mergeMatchingStacksIntoHands(PathfinderMob mob, ItemEntity itemEntity) {
		ItemStack incoming = itemEntity.getItem();
		if (incoming.isEmpty()) {
			return false;
		}
		for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND }) {
			ItemStack held = mob.getItemBySlot(slot);
			if (held.isEmpty() || !ItemStack.isSameItemSameComponents(held, incoming)) {
				continue;
			}
			int space = held.getMaxStackSize() - held.getCount();
			if (space <= 0) {
				continue;
			}
			int take = Math.min(space, incoming.getCount());
			held.grow(take);
			incoming.shrink(take);
			mob.setItemSlot(slot, held);
			itemEntity.setItem(incoming);
			if (incoming.isEmpty()) {
				itemEntity.discard();
				return true;
			}
		}
		return incoming.isEmpty();
	}

	private boolean isEligibleStack(ItemStack stack) {
		return !stack.isEmpty() && this.pickupPredicate.test(stack);
	}

	private boolean isEligibleItemEntity(ItemEntity itemEntity) {
		return itemEntity.isAlive()
				&& !itemEntity.getItem().isEmpty()
				&& !itemEntity.hasPickUpDelay()
				&& isEligibleStack(itemEntity.getItem());
	}

	@Override
	public boolean canUse() {
		if (this.mob.level().isClientSide()) {
			return false;
		}

		this.targetItem = findNearestEligible();
		return this.targetItem != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetItem != null
				&& isEligibleItemEntity(this.targetItem)
				&& this.mob.isAlive()
				&& this.mob.distanceToSqr(this.targetItem)
						<= (ITEM_SEARCH_RANGE + 1.0D) * (ITEM_SEARCH_RANGE + 1.0D);
	}

	@Override
	public void start() {
		if (this.targetItem != null) {
			this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
		}
	}

	@Override
	public void stop() {
		this.targetItem = null;
	}

	@Override
	public void tick() {
		if (this.targetItem == null || !isEligibleItemEntity(this.targetItem)) {
			return;
		}

		if (this.mob.distanceToSqr(this.targetItem) < TRY_PICKUP_DISTANCE_SQ) {
			if (this.mob instanceof IronMaterialPickupMob pickupMob) {
				pickupMob.tryPickupIronItem(this.targetItem);
			}
			return;
		}

		if (this.mob.getNavigation().isDone()) {
			this.mob.getNavigation().moveTo(this.targetItem, this.speedModifier);
		}
	}

	@Nullable
	private ItemEntity findNearestEligible() {
		AABB search = this.mob.getBoundingBox().inflate(ITEM_SEARCH_RANGE);
		List<ItemEntity> nearby = this.mob.level().getEntitiesOfClass(ItemEntity.class, search, this::isEligibleItemEntity);
		ItemEntity nearest = null;
		double nearestDistSq = Double.MAX_VALUE;
		for (ItemEntity entity : nearby) {
			double dSq = this.mob.distanceToSqr(entity);
			if (dSq < nearestDistSq) {
				nearestDistSq = dSq;
				nearest = entity;
			}
		}
		return nearest;
	}
}
