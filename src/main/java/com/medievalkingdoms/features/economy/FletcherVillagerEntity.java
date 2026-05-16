package com.medievalkingdoms.features.economy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Economy villager role: periodically produces arrows.
 *
 * <p>TODO: bow crafting / fletching table workflow (v1 only spawns arrow drops).
 */
public final class FletcherVillagerEntity extends PathfinderMob {
	private static final int ARROW_PRODUCTION_INTERVAL_TICKS = 6000;

	private int arrowProductionTicks;

	public FletcherVillagerEntity(EntityType<? extends FletcherVillagerEntity> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.25D)
				.add(Attributes.FOLLOW_RANGE, 64.0D);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel serverLevel && !serverLevel.isClientSide()) {
			this.arrowProductionTicks++;
			if (this.arrowProductionTicks >= ARROW_PRODUCTION_INTERVAL_TICKS) {
				this.arrowProductionTicks = 0;
				this.spawnArrowAtFeet(serverLevel);
			}
		}
	}

	private void spawnArrowAtFeet(ServerLevel level) {
		ItemStack stack = new ItemStack(Items.ARROW);
		double x = this.getX();
		double y = this.getY() + 0.1D;
		double z = this.getZ();
		ItemEntity drop = new ItemEntity(level, x, y, z, stack);
		drop.setDeltaMovement(Vec3.ZERO);
		drop.setPickUpDelay(10);
		level.addFreshEntity(drop);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}
}
