package com.medievalkingdoms.entity;

import com.medievalkingdoms.entity.ai.MinerDepositGoal;
import com.medievalkingdoms.entity.ai.QuarrySpiral;
import com.medievalkingdoms.entity.ai.MinerPickupDropsGoal;
import com.medievalkingdoms.entity.ai.MinerQuarryWorkGoal;
import com.medievalkingdoms.entity.ai.MinerSchedule;
import com.medievalkingdoms.entity.ai.MinerSleepGoal;
import com.medievalkingdoms.entity.ai.MinerWanderNearQuarryGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MinerEntity extends PathfinderMob {
	private static final int MINE_SHIFT_TICKS = 400;
	private static final int WANDER_LONG_TICKS = 400;
	private static final int WANDER_SHORT_TICKS = 200;
	private static final int DEPOSIT_MAX_TICKS = 6000;

	public enum DayShift {
		MINE,
		WANDER_LONG,
		DEPOSIT,
		WANDER_SHORT
	}

	private DayShift dayShift = DayShift.MINE;
	private int shiftTicksRemaining = MINE_SHIFT_TICKS;
	private int quarrySpiralIndex;
	private int quarryDepth;

	public MinerEntity(EntityType<? extends MinerEntity> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
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
		this.goalSelector.addGoal(1, new MinerSleepGoal(this));
		this.goalSelector.addGoal(2, new PanicGoal(this, 1.25D));
		this.goalSelector.addGoal(3, new MinerQuarryWorkGoal(this));
		this.goalSelector.addGoal(4, new MinerPickupDropsGoal(this, 1.0D));
		this.goalSelector.addGoal(5, new MinerDepositGoal(this));
		this.goalSelector.addGoal(6, new MinerWanderNearQuarryGoal(this));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level().isClientSide()) {
			this.tickDayShift();
		}
	}

	private void tickDayShift() {
		if (MinerSchedule.isNight(this.level())) {
			return;
		}
		if (this.isDepositShift() && !com.medievalkingdoms.features.miner.MinerLogistics.minerHasDepositItems(this)) {
			this.advanceDayShift();
			return;
		}
		if (--this.shiftTicksRemaining > 0) {
			return;
		}
		this.advanceDayShift();
	}

	private void advanceDayShift() {
		this.dayShift = switch (this.dayShift) {
			case MINE -> DayShift.WANDER_LONG;
			case WANDER_LONG -> DayShift.DEPOSIT;
			case DEPOSIT -> DayShift.WANDER_SHORT;
			case WANDER_SHORT -> DayShift.MINE;
		};
		this.shiftTicksRemaining = switch (this.dayShift) {
			case MINE -> MINE_SHIFT_TICKS;
			case WANDER_LONG -> WANDER_LONG_TICKS;
			case DEPOSIT -> DEPOSIT_MAX_TICKS;
			case WANDER_SHORT -> WANDER_SHORT_TICKS;
		};
	}

	public void finishDepositShiftEarly() {
		if (this.dayShift != DayShift.DEPOSIT) {
			return;
		}
		this.dayShift = DayShift.WANDER_SHORT;
		this.shiftTicksRemaining = WANDER_SHORT_TICKS;
	}

	public boolean isMiningShift() {
		return MinerSchedule.isDay(this.level()) && this.dayShift == DayShift.MINE;
	}

	public boolean isDepositShift() {
		return MinerSchedule.isDay(this.level()) && this.dayShift == DayShift.DEPOSIT;
	}

	public boolean isWanderShift() {
		return MinerSchedule.isDay(this.level())
				&& (this.dayShift == DayShift.WANDER_LONG || this.dayShift == DayShift.WANDER_SHORT);
	}

	public int getQuarrySpiralIndex() {
		return this.quarrySpiralIndex;
	}

	public void advanceQuarryCursor() {
		this.quarrySpiralIndex++;
		if (this.quarrySpiralIndex >= QuarrySpiral.cellCount()) {
			this.quarrySpiralIndex = 0;
		}
	}

	public int getQuarryDepth() {
		return this.quarryDepth;
	}

	public void advanceQuarryDepth() {
		this.quarryDepth++;
	}

	@Override
	public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack stack) {
		return MinerPickupDropsGoal.isMinerDrop(stack) && super.wantsToPickUp(serverLevel, stack);
	}

	@Override
	protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
		if (!itemEntity.getItem().isEmpty() && this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
			if (this.mergeIntoOffhand(itemEntity)) {
				return;
			}
		}
		super.pickUpItem(serverLevel, itemEntity);
	}

	public void tryPickupGroundDrop(ItemEntity itemEntity) {
		if (this.level() instanceof ServerLevel serverLevel
				&& !itemEntity.hasPickUpDelay()
				&& this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
			this.pickUpItem(serverLevel, itemEntity);
		}
	}

	public boolean isNearQuarryWorkSite() {
		return MinerPickupDropsGoal.isNearQuarryWorkSite(this.level(), this.position());
	}

	private boolean mergeIntoOffhand(ItemEntity itemEntity) {
		ItemStack incoming = itemEntity.getItem();
		if (incoming.isEmpty()) {
			return false;
		}
		ItemStack offhand = this.getItemBySlot(EquipmentSlot.OFFHAND);
		if (offhand.isEmpty()) {
			this.setItemSlot(EquipmentSlot.OFFHAND, incoming.split(incoming.getCount()));
			if (incoming.isEmpty()) {
				itemEntity.discard();
			} else {
				itemEntity.setItem(incoming);
			}
			return incoming.isEmpty();
		}
		if (!ItemStack.isSameItemSameComponents(offhand, incoming)) {
			return false;
		}
		int space = offhand.getMaxStackSize() - offhand.getCount();
		if (space <= 0) {
			return false;
		}
		int take = Math.min(space, incoming.getCount());
		offhand.grow(take);
		incoming.shrink(take);
		this.setItemSlot(EquipmentSlot.OFFHAND, offhand);
		itemEntity.setItem(incoming);
		if (incoming.isEmpty()) {
			itemEntity.discard();
		}
		return incoming.isEmpty();
	}
}
