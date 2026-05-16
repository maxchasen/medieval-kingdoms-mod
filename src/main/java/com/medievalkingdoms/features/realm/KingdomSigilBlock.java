package com.medievalkingdoms.features.realm;

import java.util.UUID;

import com.medievalkingdoms.features.faction.VillagerKingdom;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Placed by players to found a kingdom; breaks with very high resistance (PRD §6).
 */
public final class KingdomSigilBlock extends BaseEntityBlock {
	public static final MapCodec<KingdomSigilBlock> CODEC = simpleCodec(KingdomSigilBlock::new);

	public KingdomSigilBlock(BlockBehaviour.Properties settings) {
		super(settings);
	}

	public static BlockBehaviour.Properties createProperties() {
		// ~5x obsidian-adjacent feel (vanilla obsidian uses 50f, 1200f); tuned for "realm anchor" fantasy.
		return BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.strength(200.0F, 3600.0F)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops();
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KingdomSigilBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (!(placer instanceof ServerPlayer player)) {
			return;
		}
		if (!(level.getBlockEntity(pos) instanceof KingdomSigilBlockEntity sigil)) {
			return;
		}
		String hover = stack.getHoverName().getString().trim();
		if (hover.isEmpty()) {
			hover = "Unnamed";
		}
		if (hover.length() > 24) {
			hover = hover.substring(0, 24);
		}
		UUID kingdomId = KingdomWorldData.get(serverLevel).createKingdom(hover, player.getUUID());
		sigil.setKingdomData(kingdomId, hover, player.getUUID());
		VillagerKingdom.assignToKingdom(
				serverLevel,
				pos,
				kingdomId,
				VillagerKingdom.DEFAULT_HORIZONTAL_RADIUS,
				VillagerKingdom.DEFAULT_VERTICAL_RADIUS);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		dissolveIfOnServer(level, pos);
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel) {
			dissolveIfOnServer(level, pos);
		}
		super.destroy(level, pos, state);
	}

	/** Removes the kingdom from overworld data and clears villager kingdom tags in the sigil volume (best-effort). */
	private static void dissolveIfOnServer(LevelAccessor level, BlockPos pos) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		if (!(serverLevel.getBlockEntity(pos) instanceof KingdomSigilBlockEntity sigil)) {
			return;
		}
		UUID kingdomId = sigil.getKingdomId();
		KingdomWorldData.get(serverLevel).removeKingdom(kingdomId);
		VillagerKingdom.clearTaggedInVolume(
				serverLevel,
				pos,
				kingdomId,
				VillagerKingdom.DEFAULT_HORIZONTAL_RADIUS,
				VillagerKingdom.DEFAULT_VERTICAL_RADIUS);
	}
}
