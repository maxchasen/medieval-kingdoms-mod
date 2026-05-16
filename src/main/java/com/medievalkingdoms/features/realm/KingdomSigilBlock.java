package com.medievalkingdoms.features.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.medievalkingdoms.features.alliance.AllianceOpenUiPayload;
import com.medievalkingdoms.features.alliance.AllyRow;
import com.medievalkingdoms.features.alliance.NameKingdomUiPayload;
import com.medievalkingdoms.features.faction.VillagerKingdom;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.Direction;

/**
 * Placed by players to found a kingdom; breaks with very high resistance (PRD §6).
 */
public final class KingdomSigilBlock extends BaseEntityBlock {
	public static final MapCodec<KingdomSigilBlock> CODEC = simpleCodec(KingdomSigilBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	public KingdomSigilBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
	}

	public static BlockBehaviour.Properties createProperties() {
		// ~5x obsidian break time in survival (vanilla obsidian destroy time 50f).
		return BlockBehaviour.Properties.of()
				.mapColor(MapColor.GOLD)
				.strength(250.0F, 3600.0F)
				.sound(SoundType.WOOD)
				.requiresCorrectToolForDrops()
				.noOcclusion();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/**
	 * Owning player: first opens kingdom naming, later the alliance table (empty main hand or sneak).
	 * Wired from {@link com.medievalkingdoms.features.alliance.AllianceUiFeature} via {@code UseBlockCallback}.
	 */
	public static InteractionResult allianceTableUse(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.PASS;
		}
		if (!player.getMainHandItem().isEmpty() && !player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (!(level.getBlockState(pos).getBlock() instanceof KingdomSigilBlock)) {
			return InteractionResult.PASS;
		}
		if (!(level.getBlockEntity(pos) instanceof KingdomSigilBlockEntity sigil)) {
			return InteractionResult.PASS;
		}
		UUID owner = sigil.getOwner();
		if (owner == null || !owner.equals(player.getUUID())) {
			return InteractionResult.PASS;
		}
		UUID kingdomId = sigil.getKingdomId();
		if (kingdomId == null) {
			ServerPlayNetworking.send(serverPlayer, new NameKingdomUiPayload(pos));
			return InteractionResult.SUCCESS;
		}
		ServerLevel serverLevel = (ServerLevel) level;
		KingdomWorldData data = KingdomWorldData.get(serverLevel);
		List<AllyRow> allies = new ArrayList<>();
		data.getKingdom(kingdomId).ifPresent(entry -> {
			for (UUID allyId : entry.alliedKingdomIds()) {
				data.getKingdom(allyId).ifPresent(a -> allies.add(new AllyRow(allyId, a.internalName())));
			}
		});
		ServerPlayNetworking.send(serverPlayer, new AllianceOpenUiPayload(pos, kingdomId, allies));
		return InteractionResult.SUCCESS;
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
		if (level.isClientSide() || !(level instanceof ServerLevel)) {
			return;
		}
		if (!(placer instanceof ServerPlayer player)) {
			return;
		}
		if (!(level.getBlockEntity(pos) instanceof KingdomSigilBlockEntity sigil)) {
			return;
		}
		sigil.setPlacedPendingName(player.getUUID());
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
		if (kingdomId == null) {
			return;
		}
		KingdomWorldData.get(serverLevel).removeKingdom(kingdomId);
		VillagerKingdom.clearTaggedInVolume(
				serverLevel,
				pos,
				kingdomId,
				VillagerKingdom.DEFAULT_HORIZONTAL_RADIUS,
				VillagerKingdom.DEFAULT_VERTICAL_RADIUS);
	}
}
