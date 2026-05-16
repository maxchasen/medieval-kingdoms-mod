package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Short break between quarry shifts: wander near the active quarry marker. */
public final class MinerWanderNearQuarryGoal extends Goal {
	private static final int QUARRY_SEARCH = 48;
	private static final double WANDER_RADIUS = 10.0D;

	private final MinerEntity miner;
	private int nextMoveTick;

	public MinerWanderNearQuarryGoal(MinerEntity miner) {
		this.miner = miner;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.miner.level().isClientSide()) {
			return false;
		}
		if (!MinerSchedule.isDay(this.miner.level()) || !this.miner.isWanderShift()) {
			return false;
		}
		return findNearestQuarry() != null;
	}

	@Override
	public boolean canContinueToUse() {
		return canUse();
	}

	@Override
	public void tick() {
		if (this.miner.tickCount < this.nextMoveTick) {
			return;
		}
		this.nextMoveTick = this.miner.tickCount + 20 + this.miner.getRandom().nextInt(20);
		BlockPos quarry = findNearestQuarry();
		if (quarry == null) {
			return;
		}
		Vec3 center = Vec3.atBottomCenterOf(quarry);
		Vec3 target = DefaultRandomPos.getPosTowards(this.miner, (int) WANDER_RADIUS, 4, center, WANDER_RADIUS);
		if (target != null) {
			this.miner.getNavigation().moveTo(target.x, target.y, target.z, 0.6D);
		}
	}

	@org.jspecify.annotations.Nullable
	private BlockPos findNearestQuarry() {
		BlockPos center = this.miner.blockPosition();
		Level level = this.miner.level();
		BlockPos nearest = null;
		int best = Integer.MAX_VALUE;
		BlockPos min = center.offset(-QUARRY_SEARCH, -12, -QUARRY_SEARCH);
		BlockPos max = center.offset(QUARRY_SEARCH, 12, QUARRY_SEARCH);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (!level.getBlockState(pos).is(ModBlocks.QUARRY_BLOCK)) {
				continue;
			}
			int d = (int) center.distSqr(pos);
			if (d < best) {
				best = d;
				nearest = pos.immutable();
			}
		}
		return nearest;
	}
}
