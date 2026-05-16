package com.medievalkingdoms.entity;

import com.medievalkingdoms.entity.ai.MinerExcavationGoal;
import com.medievalkingdoms.entity.ai.MoveTowardsNearestQuarryGoal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MinerEntity extends PathfinderMob {
	public MinerEntity(EntityType<? extends MinerEntity> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.FOLLOW_RANGE, 64.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MoveTowardsNearestQuarryGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new MinerExcavationGoal(this));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}
}
