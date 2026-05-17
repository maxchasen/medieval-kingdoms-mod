package com.medievalkingdoms.features.village;

import org.jspecify.annotations.Nullable;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.combat.ModCombatEntityTypes;
import com.medievalkingdoms.features.combat.entity.ArcherEntity;
import com.medievalkingdoms.features.combat.entity.KnightEntity;
import com.medievalkingdoms.features.economy.VillagerProfessionGoals;
import com.medievalkingdoms.features.faction.KingdomMobLabels;
import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.medievalkingdoms.registry.ModEntityTypes;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;

/** Applies mod roles: combat/miner entities replace villagers; economy roles use vanilla professions + trades. */
public final class VillageRoleApplicator {
	private VillageRoleApplicator() {
	}

	public static boolean tryApplyStoredRole(Villager villager) {
		if (villager.isBaby()) {
			return false;
		}
		return MedievalKingdomsMobTags.readVillageRole(villager)
				.map(role -> applyRole(villager, role))
				.orElse(false);
	}

	public static boolean applyRole(Villager villager, String roleId) {
		ResourceKey<VillagerProfession> profession = professionForRole(roleId);
		if (profession != null) {
			return applyVanillaProfession(villager, profession);
		}
		return replaceWithRole(villager, roleId);
	}

	public static boolean applyVanillaProfession(Villager villager, ResourceKey<VillagerProfession> profession) {
		if (villager.level().isClientSide()) {
			return false;
		}
		Holder<VillagerType> type = villager.getVillagerData().type();
		Holder<VillagerProfession> prof = villager.registryAccess()
				.lookupOrThrow(Registries.VILLAGER_PROFESSION)
				.getOrThrow(profession);
		villager.setVillagerData(new VillagerData(type, prof, Math.max(1, villager.getVillagerData().level())));
		villager.setCanPickUpLoot(true);
		MedievalKingdomsMobTags.removeVillageRole(villager);
		VillagerProfessionGoals.ensureRegistered(villager);
		if (villager.level() instanceof ServerLevel serverLevel) {
			KingdomMobLabels.refreshForEntity(villager, serverLevel);
		}
		return true;
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
	public static ResourceKey<VillagerProfession> professionForRole(String roleId) {
		return switch (roleId) {
			case "smith" -> VillagerProfession.WEAPONSMITH;
			case "armorsmith" -> VillagerProfession.ARMORER;
			case "fletcher" -> VillagerProfession.FLETCHER;
			default -> null;
		};
	}

	public static Optional<String> roleFromVillagerProfession(Villager villager) {
		var profession = villager.getVillagerData().profession();
		if (profession.is(VillagerProfession.WEAPONSMITH)) {
			return Optional.of("smith");
		}
		if (profession.is(VillagerProfession.ARMORER)) {
			return Optional.of("armorsmith");
		}
		if (profession.is(VillagerProfession.FLETCHER)) {
			return Optional.of("fletcher");
		}
		return Optional.empty();
	}

	@Nullable
	private static EntityType<? extends PathfinderMob> resolveEntityType(String roleId) {
		return switch (roleId) {
			case "knight" -> ModCombatEntityTypes.KNIGHT;
			case "archer" -> ModCombatEntityTypes.ARCHER;
			case "miner" -> ModEntityTypes.MINER;
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
		if (entity instanceof Villager villager) {
			return roleFromVillagerProfession(villager);
		}
		return Optional.empty();
	}
}
