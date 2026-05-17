package com.medievalkingdoms.features.village;

import com.medievalkingdoms.entity.ai.QuarrySpiral;
import com.medievalkingdoms.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/** Places a quarry marker, outer fence ring, and deposit chest near villages once. */
public final class VillageQuarryPlacer {
	private static final int MIN_VILLAGERS = 3;
	private static final int VILLAGE_RADIUS = 64;
	private static final int QUARRY_SEARCH_RADIUS = 96;
	/** One block outside the 9×9 dig footprint (radius 4 → ring at 5). */
	private static final int FENCE_RING = QuarrySpiral.RADIUS + 1;

	private VillageQuarryPlacer() {
	}

	public static void tryPlaceNearVillage(ServerLevel level, Villager anchor) {
		if (anchor.isBaby()) {
			return;
		}
		List<Villager> villagers = level.getEntitiesOfClass(
				Villager.class, anchor.getBoundingBox().inflate(VILLAGE_RADIUS), Villager::isAlive);
		if (villagers.size() < MIN_VILLAGERS) {
			return;
		}
		BlockPos center = averageBlockPos(villagers);
		if (hasQuarryNearby(level, center)) {
			return;
		}
		BlockPos site = findQuarrySite(level, center, anchor.getRandom().nextInt(4));
		if (site == null) {
			return;
		}
		placeQuarryStructure(level, site);
	}

	private static BlockPos averageBlockPos(List<Villager> villagers) {
		long sx = 0;
		long sy = 0;
		long sz = 0;
		for (Villager villager : villagers) {
			BlockPos p = villager.blockPosition();
			sx += p.getX();
			sy += p.getY();
			sz += p.getZ();
		}
		int n = villagers.size();
		return new BlockPos((int) (sx / n), (int) (sy / n), (int) (sz / n));
	}

	private static boolean hasQuarryNearby(ServerLevel level, BlockPos center) {
		BlockPos min = center.offset(-QUARRY_SEARCH_RADIUS, -16, -QUARRY_SEARCH_RADIUS);
		BlockPos max = center.offset(QUARRY_SEARCH_RADIUS, 16, QUARRY_SEARCH_RADIUS);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (level.getBlockState(pos).is(ModBlocks.QUARRY_BLOCK)) {
				return true;
			}
		}
		return false;
	}

	private static BlockPos findQuarrySite(ServerLevel level, BlockPos center, int quadrant) {
		int[] offsets = switch (quadrant) {
			case 0 -> new int[] { 28, 28 };
			case 1 -> new int[] { -28, 28 };
			case 2 -> new int[] { -28, -28 };
			default -> new int[] { 28, -28 };
		};
		for (int ring = 0; ring < 4; ring++) {
			int dx = offsets[0] + ring * 4;
			int dz = offsets[1] + ring * 4;
			BlockPos probe = center.offset(dx, 0, dz);
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, probe.getX(), probe.getZ());
			BlockPos surface = new BlockPos(probe.getX(), y, probe.getZ());
			BlockPos below = surface.below();
			if (level.getBlockState(below).isSolidRender() && level.getBlockState(surface).isAir()) {
				return surface;
			}
		}
		return null;
	}

	private static void placeQuarryStructure(ServerLevel level, BlockPos center) {
		level.setBlockAndUpdate(center, ModBlocks.QUARRY_BLOCK.defaultBlockState());
		for (int x = -FENCE_RING; x <= FENCE_RING; x++) {
			for (int z = -FENCE_RING; z <= FENCE_RING; z++) {
				if (Math.max(Math.abs(x), Math.abs(z)) != FENCE_RING) {
					continue;
				}
				BlockPos fencePos = center.offset(x, 1, z);
				if (level.getBlockState(fencePos).isAir()) {
					level.setBlockAndUpdate(fencePos, Blocks.OAK_FENCE.defaultBlockState());
				}
			}
		}
		BlockPos chestPos = center.offset(FENCE_RING + 1, 0, 0);
		BlockState chest = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST);
		level.setBlockAndUpdate(chestPos, chest);
	}
}
