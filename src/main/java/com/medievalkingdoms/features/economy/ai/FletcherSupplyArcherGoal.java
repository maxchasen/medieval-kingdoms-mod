package com.medievalkingdoms.features.economy.ai;

import com.medievalkingdoms.features.combat.entity.ArcherEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Fletcher walks to a nearby archer and drops arrows for them to collect. */
public final class FletcherSupplyArcherGoal extends Goal {
	private static final double ARCHER_SEARCH_RANGE = 32.0D;
	private static final double SUPPLY_REACH_SQ = 9.0D;
	private static final int COOLDOWN_TICKS = 120;

	private final Villager fletcher;
	private final double speedModifier;
	private ArcherEntity targetArcher;
	private int cooldownTicks;

	public FletcherSupplyArcherGoal(Villager fletcher, double speedModifier) {
		this.fletcher = fletcher;
		this.speedModifier = speedModifier;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (this.fletcher.level().isClientSide()) {
			return false;
		}
		if (this.cooldownTicks > 0) {
			return false;
		}
		this.targetArcher = findNearestArcher();
		return this.targetArcher != null;
	}

	@Override
	public boolean canContinueToUse() {
		return this.targetArcher != null
				&& this.targetArcher.isAlive()
				&& this.fletcher.distanceToSqr(this.targetArcher) <= ARCHER_SEARCH_RANGE * ARCHER_SEARCH_RANGE;
	}

	@Override
	public void start() {
		this.moveTowardArcher();
	}

	@Override
	public void stop() {
		this.targetArcher = null;
		this.fletcher.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.cooldownTicks > 0) {
			this.cooldownTicks--;
		}
		if (this.targetArcher == null) {
			return;
		}
		this.fletcher.getLookControl().setLookAt(this.targetArcher);
		if (this.fletcher.distanceToSqr(this.targetArcher) > SUPPLY_REACH_SQ) {
			if (this.fletcher.getNavigation().isDone()) {
				this.moveTowardArcher();
			}
			return;
		}
		this.dropArrowsForArcher();
		this.cooldownTicks = COOLDOWN_TICKS;
		this.targetArcher = null;
	}

	private void moveTowardArcher() {
		if (this.targetArcher == null) {
			return;
		}
		Path path = this.fletcher.getNavigation().createPath(this.targetArcher, 0);
		if (path != null) {
			this.fletcher.getNavigation().moveTo(path, this.speedModifier);
		}
	}

	private void dropArrowsForArcher() {
		if (!(this.fletcher.level() instanceof ServerLevel level) || this.targetArcher == null) {
			return;
		}
		int count = 1 + this.fletcher.getRandom().nextInt(2);
		Vec3 dropPos = this.targetArcher.position().add(0.0D, 0.25D, 0.0D);
		ItemStack stack = new ItemStack(Items.ARROW, count);
		ItemEntity drop = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
		drop.setDeltaMovement(Vec3.ZERO);
		drop.setPickUpDelay(10);
		level.addFreshEntity(drop);
	}

	private ArcherEntity findNearestArcher() {
		List<ArcherEntity> archers = this.fletcher.level().getEntitiesOfClass(
				ArcherEntity.class,
				this.fletcher.getBoundingBox().inflate(ARCHER_SEARCH_RANGE),
				ArcherEntity::isAlive);
		ArcherEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (ArcherEntity archer : archers) {
			double d = this.fletcher.distanceToSqr(archer);
			if (d < best) {
				best = d;
				nearest = archer;
			}
		}
		return nearest;
	}
}
