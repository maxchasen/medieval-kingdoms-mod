package com.medievalkingdoms.features.crown;

import com.medievalkingdoms.features.realm.KingdomWorldData;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Paths toward the nearest warlord-crowned kingdom owner within range (PRD §5.1).
 * Disengages while the mob has an attack target or is reacting to damage.
 */
public final class CrownFollowGoal extends Goal {
	private static final double MAX_RANGE = 32.0D;
	private static final double MAX_RANGE_SQ = MAX_RANGE * MAX_RANGE;
	private static final double STOP_NEAR_SQ = 6.0D * 6.0D;
	private static final double SPEED = 1.0D;

	private final PathfinderMob mob;
	private ServerPlayer followTarget;

	public CrownFollowGoal(PathfinderMob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.mob.level().isClientSide()) {
			return false;
		}
		if (!(this.mob.level() instanceof ServerLevel sl)) {
			return false;
		}
		if (!mayFollowWarlord()) {
			return false;
		}
		ServerPlayer player = findNearestCrownedOwner(sl);
		if (player == null) {
			return false;
		}
		return this.mob.distanceToSqr(player) > STOP_NEAR_SQ;
	}

	@Override
	public boolean canContinueToUse() {
		if (this.mob.level().isClientSide()) {
			return false;
		}
		if (!(this.mob.level() instanceof ServerLevel sl)) {
			return false;
		}
		if (!mayFollowWarlord()) {
			return false;
		}
		if (this.followTarget == null || !this.followTarget.isAlive()) {
			return false;
		}
		if (this.mob.distanceToSqr(this.followTarget) > MAX_RANGE_SQ) {
			return false;
		}
		if (!wearsWarlordCrown(this.followTarget)) {
			return false;
		}
		if (!KingdomWorldData.get(sl).isKingdomOwner(this.followTarget.getUUID())) {
			return false;
		}
		return this.mob.distanceToSqr(this.followTarget) > STOP_NEAR_SQ;
	}

	@Override
	public void start() {
		if (this.mob.level() instanceof ServerLevel sl) {
			this.followTarget = findNearestCrownedOwner(sl);
		}
	}

	@Override
	public void stop() {
		this.followTarget = null;
		this.mob.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (this.followTarget != null) {
			this.mob.getNavigation().moveTo(this.followTarget, SPEED);
		}
	}

	private boolean mayFollowWarlord() {
		return this.mob.getTarget() == null && this.mob.hurtTime <= 0;
	}

	private ServerPlayer findNearestCrownedOwner(ServerLevel level) {
		AABB box = this.mob.getBoundingBox().inflate(MAX_RANGE, MAX_RANGE, MAX_RANGE);
		List<Player> candidates = level.getEntitiesOfClass(Player.class, box, p -> true);
		ServerPlayer best = null;
		double bestDist = MAX_RANGE_SQ;
		KingdomWorldData data = KingdomWorldData.get(level);
		for (Player p : candidates) {
			if (!(p instanceof ServerPlayer sp) || !sp.isAlive()) {
				continue;
			}
			if (!wearsWarlordCrown(sp)) {
				continue;
			}
			if (!data.isKingdomOwner(sp.getUUID())) {
				continue;
			}
			double d = this.mob.distanceToSqr(sp);
			if (d <= bestDist) {
				bestDist = d;
				best = sp;
			}
		}
		return best;
	}

	private static boolean wearsWarlordCrown(ServerPlayer player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(CrownFeature.WARLORDS_CROWN);
	}
}
