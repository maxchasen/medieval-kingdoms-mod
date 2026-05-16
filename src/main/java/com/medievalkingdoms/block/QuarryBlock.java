package com.medievalkingdoms.block;

import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class QuarryBlock extends FallingBlock {
	public QuarryBlock() {
		super(BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.strength(0.6F)
			.sound(SoundType.STONE));
	}
}
