package com.medievalkingdoms.features.faction;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;

/**
 * Assigns nearby villagers to a kingdom sigil territory using {@link MedievalKingdomsMobTags}.
 */
public final class VillagerKingdom {
	public static final int DEFAULT_HORIZONTAL_RADIUS = 64;
	public static final int DEFAULT_VERTICAL_RADIUS = 32;

	private VillagerKingdom() {
	}

	public static void assignToKingdom(
			ServerLevel level,
			BlockPos sigilPos,
			UUID kingdomId,
			int horizontalRadius,
			int verticalRadius
	) {
		AABB volume = kingdomVolume(sigilPos, horizontalRadius, verticalRadius);
		for (Villager villager : level.getEntitiesOfClass(Villager.class, volume, v -> true)) {
			assignIfEligible(villager, kingdomId);
		}
	}

	private static void assignIfEligible(Villager villager, UUID kingdomId) {
		Optional<UUID> existing = MedievalKingdomsMobTags.readKingdomId(villager);
		if (existing.isPresent() && !existing.get().equals(kingdomId)) {
			return;
		}
		MedievalKingdomsMobTags.writeKingdomId(villager, kingdomId);
	}

	public static void clearTaggedInVolume(ServerLevel level, BlockPos sigilPos, UUID kingdomId, int horizontalRadius, int verticalRadius) {
		if (kingdomId == null) {
			return;
		}
		AABB volume = kingdomVolume(sigilPos, horizontalRadius, verticalRadius);
		for (Villager villager : level.getEntitiesOfClass(Villager.class, volume, v -> true)) {
			MedievalKingdomsMobTags.readKingdomId(villager).filter(kingdomId::equals).ifPresent(k -> MedievalKingdomsMobTags.removeKingdomId(villager));
		}
	}

	private static AABB kingdomVolume(BlockPos sigilPos, int horizontalRadius, int verticalRadius) {
		return new AABB(sigilPos).inflate(horizontalRadius, verticalRadius, horizontalRadius);
	}
}
