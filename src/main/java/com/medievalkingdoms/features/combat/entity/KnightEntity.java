package com.medievalkingdoms.features.combat.entity;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.medievalkingdoms.features.combat.ai.KnightEquipPickupGoal;
import com.medievalkingdoms.features.crown.CrownFollowGoal;
import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.medievalkingdoms.features.realm.KingdomEntry;
import com.medievalkingdoms.features.realm.KingdomWorldData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class KnightEntity extends PathfinderMob {
	public KnightEntity(EntityType<? extends KnightEntity> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 24.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new CrownFollowGoal(this));
		this.goalSelector.addGoal(2, new KnightEquipPickupGoal(this, 1.0D));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, true));
		this.targetSelector.addGoal(
				3,
				new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, factionVillagerSelector()));
	}

	private TargetingConditions.Selector factionVillagerSelector() {
		return (candidate, serverLevel) -> KnightEntity.shouldFactionTargetVillager(this, candidate, serverLevel);
	}

	private static boolean shouldFactionTargetVillager(KnightEntity knight, LivingEntity candidate, ServerLevel level) {
		Optional<UUID> knightKingdom = MedievalKingdomsMobTags.readKingdomId(knight);
		Optional<UUID> villagerKingdom = MedievalKingdomsMobTags.readKingdomId(candidate);
		if (knightKingdom.isEmpty() || villagerKingdom.isEmpty()) {
			return false;
		}
		UUID ak = knightKingdom.get();
		UUID bk = villagerKingdom.get();
		if (ak.equals(bk)) {
			return false;
		}
		return !mutuallyAllied(level, ak, bk);
	}

	private static boolean mutuallyAllied(ServerLevel level, UUID kingdomA, UUID kingdomB) {
		Optional<KingdomEntry> ea = KingdomWorldData.get(level).getKingdom(kingdomA);
		Optional<KingdomEntry> eb = KingdomWorldData.get(level).getKingdom(kingdomB);
		if (ea.isEmpty() || eb.isEmpty()) {
			return false;
		}
		Set<UUID> alliesOfA = Set.copyOf(ea.get().alliedKingdomIds());
		Set<UUID> alliesOfB = Set.copyOf(eb.get().alliedKingdomIds());
		return alliesOfA.contains(kingdomB) && alliesOfB.contains(kingdomA);
	}
}
