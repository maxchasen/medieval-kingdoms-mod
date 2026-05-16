package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Finds the nearest {@link ModBlocks#QUARRY_BLOCK} within a bounded volume and pathfinds toward it.
 */
public final class MoveTowardsNearestQuarryGoal extends Goal {
	private static final int MAX_DIST = 64;
	private static final int MAX_DIST_SQ = MAX_DIST * MAX_DIST;
	private static final int HORIZONTAL_EXTENT = 24;
	private static final int VERTICAL_EXTENT = 12;
	private static final double ARRIVE_EPS = 2.25D;

	private final PathfinderMob mob;
	private final double speedModifier;
	private BlockPos quarryPos;
	private int nextScanTick;

	public MoveTowardsNearestQuarryGoal(PathfinderMob mob, double speedModifier) {
		this.mob = mob;
		this.speedModifier = speedModifier;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.mob.tickCount < this.nextScanTick) {
			return false;
		}
		this.nextScanTick = this.mob.tickCount + 20 + this.mob.getRandom().nextInt(40);
		this.quarryPos = findNearestQuarry();
		if (this.quarryPos == null) {
			return false;
		}
		return this.tryPathToQuarry();
	}

	@Override
	public boolean canContinueToUse() {
		if (this.quarryPos == null) {
			return false;
		}
		Level level = this.mob.level();
		if (!level.isLoaded(this.quarryPos) || !level.getBlockState(this.quarryPos).is(ModBlocks.QUARRY_BLOCK)) {
			return false;
		}
		if (this.mob.blockPosition().distSqr(this.quarryPos) <= ARRIVE_EPS) {
			return false;
		}
		return this.mob.getNavigation().isInProgress();
	}

	@Override
	public void start() {
		this.tryPathToQuarry();
	}

	@Override
	public void stop() {
		this.quarryPos = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.quarryPos != null && !this.mob.getNavigation().isInProgress()) {
			this.tryPathToQuarry();
		}
	}

	private boolean tryPathToQuarry() {
		if (this.quarryPos == null) {
			return false;
		}
		Vec3 target = Vec3.atBottomCenterOf(this.quarryPos);
		Path path = this.mob.getNavigation().createPath(this.quarryPos, 0);
		if (path != null && path.canReach()) {
			return this.mob.getNavigation().moveTo(path, this.speedModifier);
		}
		Vec3 walkToward = DefaultRandomPos.getPosTowards(this.mob, 16, 7, target, (float) Math.PI / 10.0F);
		if (walkToward == null) {
			return false;
		}
		return this.mob.getNavigation().moveTo(walkToward.x, walkToward.y, walkToward.z, this.speedModifier);
	}

	private BlockPos findNearestQuarry() {
		BlockPos center = this.mob.blockPosition();
		Level level = this.mob.level();
		BlockPos nearest = null;
		int bestDistSq = Integer.MAX_VALUE;
		BlockPos min = center.offset(-HORIZONTAL_EXTENT, -VERTICAL_EXTENT, -HORIZONTAL_EXTENT);
		BlockPos max = center.offset(HORIZONTAL_EXTENT, VERTICAL_EXTENT, HORIZONTAL_EXTENT);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			int d2 = (int) pos.distSqr(center);
			if (d2 > MAX_DIST_SQ) {
				continue;
			}
			if (!level.isLoaded(pos)) {
				continue;
			}
			if (!level.getBlockState(pos).is(ModBlocks.QUARRY_BLOCK)) {
				continue;
			}
			if (!level.getBlockState(pos.below()).isPathfindable(PathComputationType.LAND)) {
				continue;
			}
			if (d2 < bestDistSq) {
				bestDistSq = d2;
				nearest = pos.immutable();
			}
		}
		return nearest;
	}
}
