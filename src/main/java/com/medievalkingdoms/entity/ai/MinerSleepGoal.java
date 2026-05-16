package com.medievalkingdoms.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import org.jspecify.annotations.Nullable;

/** At night, path to the nearest bed and sleep like a villager. */
public final class MinerSleepGoal extends Goal {
	private static final int BED_SEARCH_RADIUS = 20;
	private static final double BED_REACH_SQ = 2.5D;

	private final PathfinderMob mob;
	@Nullable
	private BlockPos bedPos;

	public MinerSleepGoal(PathfinderMob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.mob.level().isClientSide()) {
			return false;
		}
		if (!MinerSchedule.isNight(this.mob.level())) {
			return false;
		}
		if (this.mob.isSleeping()) {
			return true;
		}
		this.bedPos = findNearestBed();
		return this.bedPos != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (!MinerSchedule.isNight(this.mob.level())) {
			return false;
		}
		return this.mob.isSleeping() || this.bedPos != null;
	}

	@Override
	public void start() {
		if (this.bedPos != null && !this.mob.isSleeping()) {
			this.mob.getNavigation().moveTo(this.bedPos.getX(), this.bedPos.getY(), this.bedPos.getZ(), 1.0D);
		}
	}

	@Override
	public void stop() {
		this.bedPos = null;
		if (this.mob.isSleeping()) {
			this.mob.stopSleeping();
		}
	}

	@Override
	public void tick() {
		if (this.bedPos == null) {
			return;
		}
		if (this.mob.isSleeping()) {
			return;
		}
		double distSq = this.mob.distanceToSqr(this.bedPos.getX() + 0.5, this.bedPos.getY() + 0.5, this.bedPos.getZ() + 0.5);
		if (distSq <= BED_REACH_SQ && this.mob.level().getBlockState(this.bedPos).is(BlockTags.BEDS)) {
			this.mob.startSleeping(this.bedPos);
			return;
		}
		if (!this.mob.getNavigation().isInProgress()) {
			Path path = this.mob.getNavigation().createPath(this.bedPos, 0);
			if (path != null) {
				this.mob.getNavigation().moveTo(path, 1.0D);
			}
		}
	}

	@Nullable
	private BlockPos findNearestBed() {
		BlockPos origin = this.mob.blockPosition();
		AABB box = new AABB(origin).inflate(BED_SEARCH_RADIUS);
		BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
		BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockState state = this.mob.level().getBlockState(pos);
			if (!state.is(BlockTags.BEDS) || !(state.getBlock() instanceof BedBlock)) {
				continue;
			}
			if (state.getValue(BedBlock.OCCUPIED)) {
				continue;
			}
			double d = origin.distSqr(pos);
			if (d < bestDist) {
				bestDist = d;
				best = pos.immutable();
			}
		}
		return best;
	}
}
