package com.medievalkingdoms.entity.ai;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;

/** Inside-out spiral cell offsets for a square quarry footprint (9×9 at radius 4). */
public final class QuarrySpiral {
	public static final int RADIUS = 4;

	private static final List<int[]> SPIRAL_OFFSETS = buildInsideOutRings(RADIUS);

	private QuarrySpiral() {
	}

	public static int cellCount() {
		return SPIRAL_OFFSETS.size();
	}

	public static int[] offset(int cellIndex) {
		return SPIRAL_OFFSETS.get(Math.floorMod(cellIndex, SPIRAL_OFFSETS.size()));
	}

	/** Center column left solid as a staircase between levels. */
	public static boolean isStairColumn(int localX, int localZ) {
		return localX == 0 && localZ == 0;
	}

	public static BlockPos cellWorldPos(BlockPos quarrySurface, int depthBelowQuarry, int cellIndex) {
		int[] off = offset(cellIndex);
		return quarrySurface.offset(off[0], -1 - depthBelowQuarry, off[1]);
	}

	private static List<int[]> buildInsideOutRings(int radius) {
		List<int[]> cells = new ArrayList<>();
		for (int ring = 0; ring <= radius; ring++) {
			for (int x = -ring; x <= ring; x++) {
				for (int z = -ring; z <= ring; z++) {
					if (Math.max(Math.abs(x), Math.abs(z)) != ring) {
						continue;
					}
					if (!isStairColumn(x, z)) {
						cells.add(new int[] { x, z });
					}
				}
			}
		}
		return cells;
	}
}
