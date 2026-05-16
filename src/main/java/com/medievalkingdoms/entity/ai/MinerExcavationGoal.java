package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.MedievalKingdomsMod;
import com.medievalkingdoms.features.miner.MinerLogistics;
import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * When close to a quarry marker, periodically breaks the top-most mineable block in the vertical column under that quarry's XZ (PRD §3.2 v1).
 */
public final class MinerExcavationGoal extends Goal {
	public static final TagKey<Block> MINER_MINEABLE =
			TagKey.create(Registries.BLOCK, MedievalKingdomsMod.id("miner_mineable"));

	private static final int MAX_DIST = 64;
	private static final int MAX_DIST_SQ = MAX_DIST * MAX_DIST;
	private static final int HORIZONTAL_EXTENT = 24;
	private static final int VERTICAL_EXTENT = 12;
	/** Squared distance from mob to quarry block center (~4 blocks). */
	private static final double NEAR_QUARRY_CENTER_DIST_SQ = 16.0D;

	private final PathfinderMob mob;
	private BlockPos quarryPos;
	private int nextExcavateTick;
	private int nextQuarryScanTick;

	public MinerExcavationGoal(PathfinderMob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.noneOf(Goal.Flag.class));
	}

	@Override
	public boolean canUse() {
		if (this.mob.level().isClientSide()) {
			return false;
		}
		if (this.mob.tickCount < this.nextQuarryScanTick) {
			return false;
		}
		this.nextQuarryScanTick = this.mob.tickCount + 20 + this.mob.getRandom().nextInt(40);
		this.quarryPos = this.findNearestQuarryWithinExcavationRange();
		return this.quarryPos != null;
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
		return this.nearQuarryCenter(this.quarryPos);
	}

	@Override
	public void start() {
		this.scheduleNextExcavateTick();
	}

	@Override
	public void stop() {
		this.quarryPos = null;
	}

	@Override
	public void tick() {
		if (this.quarryPos == null || this.mob.tickCount < this.nextExcavateTick) {
			return;
		}
		this.scheduleNextExcavateTick();
		this.tryBreakColumnBlock();
	}

	private void scheduleNextExcavateTick() {
		this.nextExcavateTick = this.mob.tickCount + 12 + this.mob.getRandom().nextInt(9);
	}

	private boolean nearQuarryCenter(BlockPos quarry) {
		return this.mob.position().distanceToSqr(Vec3.atBottomCenterOf(quarry)) <= NEAR_QUARRY_CENTER_DIST_SQ;
	}

	private BlockPos findNearestQuarryWithinExcavationRange() {
		BlockPos center = this.mob.blockPosition();
		Level level = this.mob.level();
		BlockPos nearest = null;
		double bestDistSq = Double.MAX_VALUE;
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
			if (!this.nearQuarryCenter(pos)) {
				continue;
			}
			double mobDistSq = this.mob.position().distanceToSqr(Vec3.atBottomCenterOf(pos));
			if (mobDistSq < bestDistSq) {
				bestDistSq = mobDistSq;
				nearest = pos.immutable();
			}
		}
		return nearest;
	}

	private void tryBreakColumnBlock() {
		if (this.quarryPos == null || this.mob.level().isClientSide()) {
			return;
		}
		Level level = this.mob.level();
		int qx = this.quarryPos.getX();
		int qz = this.quarryPos.getZ();
		int quarryY = this.quarryPos.getY();
		BlockPos.MutableBlockPos probe = this.quarryPos.mutable();
		for (int y = quarryY; y >= quarryY - 48; y--) {
			probe.set(qx, y, qz);
			if (!level.isLoaded(probe)) {
				break;
			}
			BlockState state = level.getBlockState(probe);
			if (!state.isAir() && state.is(MINER_MINEABLE)) {
				if (level instanceof ServerLevel serverLevel) {
					MinerLogistics.tryHarvestIntoNearbyStorage(serverLevel, this.mob, probe.immutable(), state);
				}
				break;
			}
		}
	}
}
