package com.medievalkingdoms.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class QuarryBlock extends FallingBlock {
	public static final MapCodec<QuarryBlock> CODEC = Block.simpleCodec(QuarryBlock::new);

	public QuarryBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public static BlockBehaviour.Properties createProperties() {
		return BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.instrument(NoteBlockInstrument.BASEDRUM)
			.strength(1.5F, 6.0F)
			.sound(SoundType.STONE)
			.requiresCorrectToolForDrops();
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec() {
		return CODEC;
	}

	@Override
	public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
		return MapColor.STONE.calculateARGBColor(MapColor.Brightness.NORMAL);
	}
}
