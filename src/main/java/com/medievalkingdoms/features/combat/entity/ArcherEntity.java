package com.medievalkingdoms.features.combat.entity;

import com.medievalkingdoms.features.combat.ai.ArcherPickupArrowsGoal;
import com.medievalkingdoms.features.combat.ai.ArcherPickupArrowsGoal;
import com.medievalkingdoms.features.crown.CrownFollowGoal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public final class ArcherEntity extends PathfinderMob implements RangedAttackMob {
	public ArcherEntity(EntityType<? extends ArcherEntity> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new CrownFollowGoal(this));
		this.goalSelector.addGoal(2, new ArcherPickupArrowsGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0D, 20, 40, 15.0F));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
	}

	@Override
	public ItemStack getProjectile(ItemStack weaponStack) {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public void performRangedAttack(LivingEntity target, float power) {
		ensureRangedLoadout();
		ItemStack bow = this.getMainHandItem();
		ItemStack projectileStack = this.getProjectile(bow);
		AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectileStack, power, bow);
		double dx = target.getX() - this.getX();
		double dy = target.getY(0.3333333333333333) - arrow.getY();
		double dz = target.getZ() - this.getZ();
		double horizontalDist = Math.sqrt(dx * dx + dz * dz);
		if (this.level() instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileUsingShoot(
					arrow, serverLevel, projectileStack, dx, dy + horizontalDist * 0.2F, dz, 1.6F, 14 - serverLevel.getDifficulty().getId() * 4);
		}

		this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	@Override
	public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack stack) {
		return stack.is(Items.ARROW) || super.wantsToPickUp(serverLevel, stack);
	}

	public void ensureRangedLoadout() {
		if (!this.getMainHandItem().is(Items.BOW)) {
			this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		}
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
			ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		return super.finalizeSpawn(level, difficulty, reason, groupData);
	}
}
