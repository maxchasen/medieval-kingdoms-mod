package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.miner.MinerLogistics;
import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Paths adjacent to the current spiral quarry cell, swings a pickaxe, and breaks blocks using vanilla dig speed.
 */
public final class MinerQuarryWorkGoal extends Goal {
	private static final int HORIZONTAL_EXTENT = 24;
	private static final int VERTICAL_EXTENT = 12;
	private static final double ADJACENT_REACH_SQ = 3.5D;

	private final MinerEntity miner;
	private BlockPos quarrySurface;
	private BlockPos targetBlock;
	private float destroyProgress;
	private int rescanCooldown;

	public MinerQuarryWorkGoal(MinerEntity miner) {
		this.miner = miner;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.miner.level().isClientSide()) {
			return false;
		}
		if (!MinerSchedule.isDay(this.miner.level()) || !this.miner.isMiningShift()) {
			return false;
		}
		this.quarrySurface = findNearestQuarry();
		if (this.quarrySurface == null) {
			return false;
		}
		this.targetBlock = findNextMineTarget();
		return this.targetBlock != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (!MinerSchedule.isDay(this.miner.level()) || !this.miner.isMiningShift()) {
			return false;
		}
		if (this.quarrySurface == null || !this.miner.level().getBlockState(this.quarrySurface).is(ModBlocks.QUARRY_BLOCK)) {
			return false;
		}
		if (this.targetBlock == null) {
			this.targetBlock = findNextMineTarget();
		}
		return this.targetBlock != null;
	}

	@Override
	public void start() {
		this.destroyProgress = 0.0F;
		this.rescanCooldown = 0;
		this.moveAdjacentToTarget();
	}

	@Override
	public void stop() {
		this.targetBlock = null;
		this.quarrySurface = null;
		this.destroyProgress = 0.0F;
		this.miner.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.targetBlock == null || this.quarrySurface == null) {
			return;
		}
		Level level = this.miner.level();
		BlockState state = level.getBlockState(this.targetBlock);
		if (state.isAir() || !state.is(MinerTags.MINER_MINEABLE)) {
			this.advanceCursor();
			this.targetBlock = findNextMineTarget();
			this.destroyProgress = 0.0F;
			this.moveAdjacentToTarget();
			return;
		}

		this.miner.getLookControl().setLookAt(
				this.targetBlock.getX() + 0.5D, this.targetBlock.getY() + 0.5D, this.targetBlock.getZ() + 0.5D);

		if (!isAdjacentToTarget()) {
			if (this.miner.getNavigation().isDone()) {
				this.moveAdjacentToTarget();
			}
			return;
		}

		this.miner.swing(InteractionHand.MAIN_HAND);
		this.destroyProgress += digProgressPerSwing(state, level, this.targetBlock);
		if (this.destroyProgress < 1.0F) {
			return;
		}

		if (level instanceof ServerLevel serverLevel) {
			MinerLogistics.harvestBlockForMiner(serverLevel, this.miner, this.targetBlock, state);
		}
		this.destroyProgress = 0.0F;
		this.advanceCursor();
		this.targetBlock = findNextMineTarget();
		this.moveAdjacentToTarget();
	}

	private void advanceCursor() {
		int cells = QuarrySpiral.cellCount();
		for (int attempt = 0; attempt < cells + 1; attempt++) {
			this.miner.advanceQuarryCursor();
			BlockPos next = peekMineTarget();
			if (next != null) {
				return;
			}
			if (this.miner.getQuarrySpiralIndex() == 0) {
				this.miner.advanceQuarryDepth();
			}
		}
	}

	private BlockPos findNextMineTarget() {
		if (this.quarrySurface == null) {
			return null;
		}
		int cells = QuarrySpiral.cellCount();
		for (int attempt = 0; attempt < cells * 4; attempt++) {
			BlockPos candidate = peekMineTarget();
			if (candidate != null) {
				return candidate;
			}
			this.miner.advanceQuarryCursor();
			if (this.miner.getQuarrySpiralIndex() == 0) {
				this.miner.advanceQuarryDepth();
			}
		}
		return null;
	}

	private BlockPos peekMineTarget() {
		if (this.quarrySurface == null) {
			return null;
		}
		BlockPos pos = QuarrySpiral.cellWorldPos(this.quarrySurface, this.miner.getQuarryDepth(), this.miner.getQuarrySpiralIndex());
		if (!this.miner.level().isLoaded(pos)) {
			return null;
		}
		BlockState state = this.miner.level().getBlockState(pos);
		if (state.isAir() || !state.is(MinerTags.MINER_MINEABLE)) {
			return null;
		}
		return pos;
	}

	private boolean isAdjacentToTarget() {
		if (this.targetBlock == null) {
			return false;
		}
		return this.miner.blockPosition().distSqr(this.targetBlock) <= ADJACENT_REACH_SQ;
	}

	private void moveAdjacentToTarget() {
		if (this.targetBlock == null) {
			return;
		}
		BlockPos stand = findStandBeside(this.targetBlock);
		Path path = this.miner.getNavigation().createPath(stand, 0);
		if (path != null) {
			this.miner.getNavigation().moveTo(path, 1.0D);
		}
	}

	private BlockPos findStandBeside(BlockPos target) {
		Level level = this.miner.level();
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos feet = target.relative(direction);
			BlockPos below = feet.below();
			if (level.getBlockState(below).isSolidRender()
					&& level.getBlockState(feet).isPathfindable(PathComputationType.LAND)) {
				return feet;
			}
		}
		return target.above();
	}

	private float digProgressPerSwing(BlockState state, Level level, BlockPos pos) {
		float hardness = state.getDestroySpeed(level, pos);
		if (hardness < 0.0F) {
			return 0.0F;
		}
		float toolSpeed = this.miner.getMainHandItem().getDestroySpeed(state);
		if (toolSpeed > 1.0F) {
			return toolSpeed / hardness / 30.0F;
		}
		return toolSpeed / hardness / 100.0F;
	}

	private BlockPos findNearestQuarry() {
		BlockPos center = this.miner.blockPosition();
		Level level = this.miner.level();
		BlockPos nearest = null;
		double best = Double.MAX_VALUE;
		BlockPos min = center.offset(-HORIZONTAL_EXTENT, -VERTICAL_EXTENT, -HORIZONTAL_EXTENT);
		BlockPos max = center.offset(HORIZONTAL_EXTENT, VERTICAL_EXTENT, HORIZONTAL_EXTENT);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (!level.getBlockState(pos).is(ModBlocks.QUARRY_BLOCK)) {
				continue;
			}
			double d = this.miner.position().distanceToSqr(Vec3.atBottomCenterOf(pos));
			if (d < best) {
				best = d;
				nearest = pos.immutable();
			}
		}
		return nearest;
	}
}
