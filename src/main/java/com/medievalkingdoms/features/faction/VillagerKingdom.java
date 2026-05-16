package com.medievalkingdoms.features.faction;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.medievalkingdoms.features.realm.KingdomSigilBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * Assigns nearby villagers and kingdom workforce to a kingdom sigil territory using {@link MedievalKingdomsMobTags}.
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
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, volume, KingdomMobLabels::isKingdomWorkforce)) {
			if (entity instanceof Villager) {
				continue;
			}
			assignWorkforceIfEligible(entity, kingdomId);
		}
		refreshNametagsInVolume(level, volume, kingdomId);
	}

	/** Periodically call from villagers so walking into territory after the sigil exists still tags them. */
	public static void tryTagFromNearbySigil(ServerLevel level, Villager villager) {
		Optional<UUID> existing = MedievalKingdomsMobTags.readKingdomId(villager);
		if (existing.isPresent()) {
			KingdomMobLabels.refreshForEntity(villager, level);
			return;
		}
		UUID kid = nearestFoundedSigilKingdom(villager, level, DEFAULT_HORIZONTAL_RADIUS, DEFAULT_VERTICAL_RADIUS);
		if (kid != null) {
			assignIfEligible(villager, kid);
		}
	}

	public static void refreshNametagsInVolume(ServerLevel level, AABB volume, UUID kingdomId) {
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, volume, KingdomMobLabels::isKingdomWorkforce)) {
			MedievalKingdomsMobTags.readKingdomId(entity)
					.filter(kingdomId::equals)
					.ifPresent(k -> KingdomMobLabels.refreshForEntity(entity, level));
		}
	}

	private static @Nullable UUID nearestFoundedSigilKingdom(Entity entity, ServerLevel level, int hRadius, int vRadius) {
		BlockPos origin = entity.blockPosition();
		double ex = entity.getX();
		double ey = entity.getY();
		double ez = entity.getZ();

		double hMax = hRadius;
		double vMax = vRadius;

		ChunkPos chunkMin = new ChunkPos(new BlockPos(origin.getX() - hRadius, 0, origin.getZ() - hRadius));
		ChunkPos chunkMax = new ChunkPos(new BlockPos(origin.getX() + hRadius, 0, origin.getZ() + hRadius));

		double bestSq = Double.POSITIVE_INFINITY;
		@Nullable UUID bestId = null;

		for (ChunkPos chunkPos : ChunkPos.rangeClosed(chunkMin, chunkMax).toList()) {
			LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
			if (chunk == null) {
				continue;
			}
			for (BlockEntity candidate : chunk.getBlockEntities().values()) {
				if (!(candidate instanceof KingdomSigilBlockEntity sigil)) {
					continue;
				}
				UUID kingdomId = sigil.getKingdomId();
				if (kingdomId == null) {
					continue;
				}
				BlockPos p = candidate.getBlockPos();
				double sx = p.getX() + 0.5;
				double sy = p.getY() + 0.5;
				double sz = p.getZ() + 0.5;
				double dx = sx - ex;
				double dz = sz - ez;
				if (dx * dx + dz * dz > hMax * hMax) {
					continue;
				}
				double dy = sy - ey;
				if (Math.abs(dy) > vMax) {
					continue;
				}
				double distSq = dx * dx + dy * dy + dz * dz;
				if (distSq < bestSq) {
					bestSq = distSq;
					bestId = kingdomId;
				}
			}
		}

		return bestId;
	}

	private static void assignIfEligible(Villager villager, UUID kingdomId) {
		Optional<UUID> existing = MedievalKingdomsMobTags.readKingdomId(villager);
		if (existing.isPresent() && !existing.get().equals(kingdomId)) {
			return;
		}
		MedievalKingdomsMobTags.writeKingdomId(villager, kingdomId);
		if (villager.level() instanceof ServerLevel serverLevel) {
			KingdomMobLabels.refreshForEntity(villager, serverLevel);
		}
	}

	private static void assignWorkforceIfEligible(LivingEntity entity, UUID kingdomId) {
		Optional<UUID> existing = MedievalKingdomsMobTags.readKingdomId(entity);
		if (existing.isPresent() && !existing.get().equals(kingdomId)) {
			return;
		}
		MedievalKingdomsMobTags.writeKingdomId(entity, kingdomId);
		if (entity.level() instanceof ServerLevel serverLevel) {
			KingdomMobLabels.refreshForEntity(entity, serverLevel);
		}
	}

	public static void clearTaggedInVolume(ServerLevel level, BlockPos sigilPos, UUID kingdomId, int horizontalRadius, int verticalRadius) {
		if (kingdomId == null) {
			return;
		}
		AABB volume = kingdomVolume(sigilPos, horizontalRadius, verticalRadius);
		for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, volume, KingdomMobLabels::isKingdomWorkforce)) {
			MedievalKingdomsMobTags.readKingdomId(entity).filter(kingdomId::equals).ifPresent(k -> {
				MedievalKingdomsMobTags.removeKingdomId(entity);
				entity.setCustomName(null);
				entity.setCustomNameVisible(false);
			});
		}
	}

	private static AABB kingdomVolume(BlockPos sigilPos, int horizontalRadius, int verticalRadius) {
		return new AABB(sigilPos).inflate(horizontalRadius, verticalRadius, horizontalRadius);
	}
}
