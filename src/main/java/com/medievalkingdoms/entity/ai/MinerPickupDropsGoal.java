package com.medievalkingdoms.entity.ai;

import java.util.EnumSet;
import java.util.List;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Paths toward quarry drop items on the ground and collects them into the miner's offhand. */
public final class MinerPickupDropsGoal extends Goal {
	public static final double ITEM_SEARCH_RANGE = 8.0D;
	private static final double TRY_PICKUP_DISTANCE_SQ = 2.25D;
	private static final double NEAR_QUARRY_CENTER_DIST_SQ = 16.0D;
	private static final int QUARRY_SEARCH_RADIUS = 24;

	private final MinerEntity miner;
	private final double speedModifier;
	@Nullable
	private ItemEntity targetItem;

	public MinerPickupDropsGoal(MinerEntity miner, double speedModifier) {
		this.miner = miner;
		this.speedModifier = speedModifier;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	public static boolean isMinerDrop(ItemStack stack) {
		return !stack.isEmpty() && !stack.is(Items.IRON_PICKAXE);
	}

	@Override
	public boolean canUse() {
		if (this.miner.level().isClientSide() || !this.miner.isNearQuarryWorkSite()) {
			return false;
		}
		this.targetItem = findNearestDrop();
		return this.targetItem != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetItem != null
				&& isEligibleItemEntity(this.targetItem)
				&& this.miner.isAlive()
				&& this.miner.isNearQuarryWorkSite()
				&& this.miner.distanceToSqr(this.targetItem)
						<= (ITEM_SEARCH_RANGE + 1.0D) * (ITEM_SEARCH_RANGE + 1.0D);
	}

	@Override
	public void start() {
		if (this.targetItem != null) {
			this.miner.getNavigation().moveTo(this.targetItem, this.speedModifier);
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
		if (this.miner.distanceToSqr(this.targetItem) < TRY_PICKUP_DISTANCE_SQ) {
			this.miner.tryPickupGroundDrop(this.targetItem);
			return;
		}
		if (this.miner.getNavigation().isDone()) {
			this.miner.getNavigation().moveTo(this.targetItem, this.speedModifier);
		}
	}

	private boolean isEligibleItemEntity(ItemEntity itemEntity) {
		return itemEntity.isAlive()
				&& !itemEntity.getItem().isEmpty()
				&& !itemEntity.hasPickUpDelay()
				&& isMinerDrop(itemEntity.getItem());
	}

	@Nullable
	private ItemEntity findNearestDrop() {
		AABB search = this.miner.getBoundingBox().inflate(ITEM_SEARCH_RANGE);
		List<ItemEntity> nearby = this.miner.level().getEntitiesOfClass(ItemEntity.class, search, this::isEligibleItemEntity);
		ItemEntity nearest = null;
		double nearestDistSq = Double.MAX_VALUE;
		for (ItemEntity entity : nearby) {
			double dSq = this.miner.distanceToSqr(entity);
			if (dSq < nearestDistSq) {
				nearestDistSq = dSq;
				nearest = entity;
			}
		}
		return nearest;
	}

	/** True when within working distance of any quarry marker (same band as excavation). */
	public static boolean isNearQuarryWorkSite(Level level, Vec3 position) {
		BlockPos center = BlockPos.containing(position);
		BlockPos min = center.offset(-QUARRY_SEARCH_RADIUS, -12, -QUARRY_SEARCH_RADIUS);
		BlockPos max = center.offset(QUARRY_SEARCH_RADIUS, 12, QUARRY_SEARCH_RADIUS);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (!level.isLoaded(pos) || !level.getBlockState(pos).is(ModBlocks.QUARRY_BLOCK)) {
				continue;
			}
			if (position.distanceToSqr(Vec3.atBottomCenterOf(pos)) <= NEAR_QUARRY_CENTER_DIST_SQ) {
				return true;
			}
		}
		return false;
	}
}
