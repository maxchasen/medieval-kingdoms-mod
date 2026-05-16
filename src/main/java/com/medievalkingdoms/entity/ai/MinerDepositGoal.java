package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.miner.MinerLogistics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** During deposit shift, path to a chest/barrel and empty gathered ore from the offhand. */
public final class MinerDepositGoal extends Goal {
	private static final double CHEST_REACH_SQ = 3.5D;

	private final MinerEntity miner;
	private BlockPos storagePos;

	public MinerDepositGoal(MinerEntity miner) {
		this.miner = miner;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.miner.level().isClientSide()) {
			return false;
		}
		if (!MinerSchedule.isDay(this.miner.level()) || !this.miner.isDepositShift()) {
			return false;
		}
		if (!MinerLogistics.minerHasDepositItems(this.miner)) {
			return false;
		}
		this.storagePos = MinerLogistics.findNearestStoragePos((ServerLevel) this.miner.level(), this.miner.position());
		return this.storagePos != null;
	}

	@Override
	public boolean canContinueToUse() {
		if (!MinerSchedule.isDay(this.miner.level()) || !this.miner.isDepositShift()) {
			return false;
		}
		if (!MinerLogistics.minerHasDepositItems(this.miner)) {
			return false;
		}
		return this.storagePos != null;
	}

	@Override
	public void start() {
		this.moveToStorage();
	}

	@Override
	public void stop() {
		this.storagePos = null;
		this.miner.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.storagePos == null) {
			return;
		}
		if (this.miner.blockPosition().distSqr(this.storagePos) > CHEST_REACH_SQ) {
			if (this.miner.getNavigation().isDone()) {
				this.moveToStorage();
			}
			return;
		}
		MinerLogistics.depositMinerItems((ServerLevel) this.miner.level(), this.miner);
		if (!MinerLogistics.minerHasDepositItems(this.miner)) {
			this.miner.finishDepositShiftEarly();
		}
	}

	private void moveToStorage() {
		if (this.storagePos == null) {
			return;
		}
		Path path = this.miner.getNavigation().createPath(this.storagePos, 1);
		if (path != null) {
			this.miner.getNavigation().moveTo(path, 1.0D);
		}
	}
}
