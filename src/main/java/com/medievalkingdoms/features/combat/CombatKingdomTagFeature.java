package com.medievalkingdoms.features.combat;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.combat.entity.ArcherEntity;
import com.medievalkingdoms.features.combat.entity.KnightEntity;
import com.medievalkingdoms.features.faction.KingdomMobLabels;
import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.medievalkingdoms.features.realm.KingdomSigilBlockEntity;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** Assigns mk_kingdom to knights/archers spawned near a sigil (PRD faction parity). */
public final class CombatKingdomTagFeature implements ModInitializer {
	/** Horizontal block radius from the mob position when searching loaded chunks for sigils. */
	public static final int SEARCH_SIGIL_HORIZONTAL_BLOCKS = 24;
	/** Vertical block radius from the mob position when selecting a sigil. */
	public static final int SEARCH_SIGIL_VERTICAL_BLOCKS = 12;

	@Override
	public void onInitialize() {
		ServerEntityEvents.ENTITY_LOAD.register(CombatKingdomTagFeature::handleEntityLoaded);
	}

	private static void handleEntityLoaded(Entity entity, ServerLevel world) {
		if (!(entity instanceof KnightEntity) && !(entity instanceof ArcherEntity) && !(entity instanceof MinerEntity)) {
			return;
		}
		if (MedievalKingdomsMobTags.readKingdomId(entity).isEmpty()) {
			@Nullable UUID nearestKingdomId = nearestSigilKingdomId(entity, world);
			if (nearestKingdomId != null) {
				MedievalKingdomsMobTags.writeKingdomId(entity, nearestKingdomId);
			}
		}
		if (entity instanceof KnightEntity knight) {
			KingdomMobLabels.refreshForEntity(knight, world);
		} else if (entity instanceof ArcherEntity archer) {
			archer.ensureRangedLoadout();
			KingdomMobLabels.refreshForEntity(archer, world);
		} else if (entity instanceof MinerEntity miner) {
			KingdomMobLabels.refreshForEntity(miner, world);
		}
	}

	private static @Nullable UUID nearestSigilKingdomId(Entity entity, ServerLevel level) {
		BlockPos origin = entity.blockPosition();
		double ex = entity.getX();
		double ey = entity.getY();
		double ez = entity.getZ();

		double hMax = SEARCH_SIGIL_HORIZONTAL_BLOCKS;
		double vMax = SEARCH_SIGIL_VERTICAL_BLOCKS;

		ChunkPos chunkMin = new ChunkPos(new BlockPos(origin.getX() - SEARCH_SIGIL_HORIZONTAL_BLOCKS, 0, origin.getZ() - SEARCH_SIGIL_HORIZONTAL_BLOCKS));
		ChunkPos chunkMax = new ChunkPos(new BlockPos(origin.getX() + SEARCH_SIGIL_HORIZONTAL_BLOCKS, 0, origin.getZ() + SEARCH_SIGIL_HORIZONTAL_BLOCKS));

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
}
