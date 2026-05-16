package com.medievalkingdoms.entity.ai;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class MinerTags {
	public static final TagKey<Block> MINER_MINEABLE =
			TagKey.create(Registries.BLOCK, MedievalKingdomsMod.id("miner_mineable"));

	private MinerTags() {
	}
}
