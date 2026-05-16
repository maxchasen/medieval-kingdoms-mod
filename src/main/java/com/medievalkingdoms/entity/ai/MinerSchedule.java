package com.medievalkingdoms.entity.ai;

import net.minecraft.world.level.Level;

/** Day/night window for miner quarry shifts (matches overworld work hours). */
public final class MinerSchedule {
	private MinerSchedule() {
	}

	public static boolean isDay(Level level) {
		long dayTime = level.getDayTime() % 24000L;
		return dayTime >= 0 && dayTime < 12000L;
	}

	public static boolean isNight(Level level) {
		return !isDay(level);
	}
}
