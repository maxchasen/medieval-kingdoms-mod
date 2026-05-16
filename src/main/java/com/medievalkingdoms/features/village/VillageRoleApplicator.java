package com.medievalkingdoms.features.village;

import org.jspecify.annotations.Nullable;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;
import com.medievalkingdoms.features.combat.entity.ArcherEntity;
import com.medievalkingdoms.features.combat.entity.KnightEntity;
import com.medievalkingdoms.features.economy.ArmorsmithVillagerEntity;
import com.medievalkingdoms.features.economy.FletcherVillagerEntity;
import com.medievalkingdoms.features.economy.ModEconomyEntityTypes;
import com.medievalkingdoms.features.economy.WeaponsmithVillagerEntity;
import com.medievalkingdoms.features.faction.KingdomMobLabels;
import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.medievalkingdoms.registry.ModEntityTypes;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.villager.Villager;

/** Replaces vanilla villagers with mod workforce entities based on rolled {@link MedievalKingdomsMobTags#VILLAGE_ROLE_ID}. */
public final class VillageRoleApplicator {
	private VillageRoleApplicator() {
	}

	public static boolean tryApplyStoredRole(Villager villager) {
		if (villager.isBaby()) {
			return false;
		}
		return MedievalKingdomsMobTags.readVillageRole(villager)
				.map(role -> replaceWithRole(villager, role))
				.orElse(false);
	}

	public static boolean replaceWithRole(Villager villager, String roleId) {
		if (!(villager.level() instanceof ServerLevel serverLevel)) {
			return false;
		}
		EntityType<? extends PathfinderMob> type = resolveEntityType(roleId);
		if (type == null) {
			return false;
		}
		PathfinderMob replacement = type.create(serverLevel, EntitySpawnReason.CONVERSION);
		if (replacement == null) {
			return false;
		}
		replacement.snapTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
		replacement.setYHeadRot(villager.getYHeadRot());
		MedievalKingdomsMobTags.readKingdomId(villager).ifPresent(id -> MedievalKingdomsMobTags.writeKingdomId(replacement, id));
		MedievalKingdomsMobTags.removeVillageRole(villager);
		serverLevel.addFreshEntity(replacement);
		KingdomMobLabels.refreshForEntity(replacement, serverLevel);
		villager.discard();
		return true;
	}

	@Nullable
	private static EntityType<? extends PathfinderMob> resolveEntityType(String roleId) {
		return switch (roleId) {
			case "knight" -> ModCombatEntityTypes.KNIGHT;
			case "archer" -> ModCombatEntityTypes.ARCHER;
			case "miner" -> ModEntityTypes.MINER;
			case "smith" -> ModEconomyEntityTypes.WEAPONSMITH_VILLAGER;
			case "armorsmith" -> ModEconomyEntityTypes.ARMORSMITH_VILLAGER;
			case "fletcher" -> ModEconomyEntityTypes.FLETCHER_VILLAGER;
			default -> null;
		};
	}

	public static boolean isModWorkforce(Entity entity) {
		return roleIdForWorkforce(entity).isPresent();
	}

	public static Optional<String> roleIdForWorkforce(Entity entity) {
		if (entity instanceof KnightEntity) {
			return Optional.of("knight");
		}
		if (entity instanceof ArcherEntity) {
			return Optional.of("archer");
		}
		if (entity instanceof MinerEntity) {
			return Optional.of("miner");
		}
		if (entity instanceof WeaponsmithVillagerEntity) {
			return Optional.of("smith");
		}
		if (entity instanceof ArmorsmithVillagerEntity) {
			return Optional.of("armorsmith");
		}
		if (entity instanceof FletcherVillagerEntity) {
			return Optional.of("fletcher");
		}
		return Optional.empty();
	}
}
