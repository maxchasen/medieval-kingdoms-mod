package com.medievalkingdoms.features.village;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.phys.AABB;

/**
 * Keeps at least one of each mod workforce role per village cluster. New spawns fill gaps first;
 * existing nitwits and unemployed villagers are converted during periodic backfill.
 */
public final class VillageRoleEnsurer {
	public static final int VILLAGE_RADIUS = 80;

	private VillageRoleEnsurer() {
	}

	public static Optional<String> pickRoleForSpawn(ServerLevel level, Villager villager) {
		Set<String> missing = findMissingRoles(level, villager);
		if (!missing.isEmpty()) {
			return pickRandom(missing, villager.getRandom());
		}
		return VillageRoleConfig.get().rollModRole(villager.getRandom()).filter(VillageRoleCatalog::isAssignable);
	}

	public static void tryBackfillMissingRoles(ServerLevel level, Villager anchor) {
		if (anchor.isBaby()) {
			return;
		}
		Set<String> missing = findMissingRoles(level, anchor);
		if (missing.isEmpty()) {
			return;
		}
		findReplacementCandidate(level, anchor).ifPresent(candidate -> {
			String role = pickRandom(missing, anchor.getRandom()).orElseThrow();
			VillageRoleApplicator.replaceWithRole(candidate, role);
		});
	}

	public static boolean isPreferredReplacementTarget(Villager villager) {
		if (villager.isBaby()) {
			return false;
		}
		if (MedievalKingdomsMobTags.readVillageRole(villager).isPresent()) {
			return false;
		}
		VillagerData data = villager.getVillagerData();
		return data.profession().is(VillagerProfession.NITWIT) || data.profession().is(VillagerProfession.NONE);
	}

	private static Set<String> findMissingRoles(ServerLevel level, Villager anchor) {
		Set<String> present = collectPresentRoles(level, anchor);
		Set<String> missing = new HashSet<>(VillageRoleCatalog.ASSIGNABLE_ROLES);
		missing.removeAll(present);
		return missing;
	}

	private static Set<String> collectPresentRoles(ServerLevel level, Villager anchor) {
		Set<String> present = new HashSet<>();
		AABB volume = cohortBounds(anchor);
		for (Villager villager : level.getEntitiesOfClass(Villager.class, volume, v -> withinCohort(anchor, v))) {
			MedievalKingdomsMobTags.readVillageRole(villager)
					.filter(VillageRoleCatalog::isAssignable)
					.ifPresent(present::add);
		}
		for (Entity entity : level.getEntitiesOfClass(Entity.class, volume, e -> withinCohort(anchor, e))) {
			VillageRoleApplicator.roleIdForWorkforce(entity).ifPresent(present::add);
		}
		return present;
	}

	private static Optional<Villager> findReplacementCandidate(ServerLevel level, Villager anchor) {
		AABB volume = cohortBounds(anchor);
		List<Villager> candidates = level.getEntitiesOfClass(Villager.class, volume, VillageRoleEnsurer::isPreferredReplacementTarget)
				.stream()
				.filter(v -> withinCohort(anchor, v))
				.sorted(Comparator.comparingInt(VillageRoleEnsurer::replacementPriority))
				.toList();
		return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.getFirst());
	}

	private static int replacementPriority(Villager villager) {
		if (villager.getVillagerData().profession().is(VillagerProfession.NITWIT)) {
			return 0;
		}
		return 1;
	}

	private static AABB cohortBounds(Villager anchor) {
		return anchor.getBoundingBox().inflate(VILLAGE_RADIUS);
	}

	private static boolean withinCohort(Villager anchor, Entity entity) {
		return anchor.position().distanceToSqr(entity.position()) <= (double) VILLAGE_RADIUS * VILLAGE_RADIUS;
	}

	private static Optional<String> pickRandom(Set<String> roles, RandomSource random) {
		if (roles.isEmpty()) {
			return Optional.empty();
		}
		List<String> list = new ArrayList<>(roles);
		return Optional.of(list.get(random.nextInt(list.size())));
	}
}
