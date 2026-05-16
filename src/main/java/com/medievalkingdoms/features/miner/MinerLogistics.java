package com.medievalkingdoms.features.miner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Deposits miner breaks into the nearest vanilla chest or barrel within range, mirroring hopper-style
 * merge rules via {@link Container#canPlaceItem}. Unfit stacks stay as world drops ({@link Block#popResource}).
 */
public final class MinerLogistics {
	private MinerLogistics() {}

	private static final int STORAGE_SEARCH_RADIUS = 12;
	private static final double STORAGE_SEARCH_RADIUS_SQ = STORAGE_SEARCH_RADIUS * STORAGE_SEARCH_RADIUS;

	public static void tryHarvestIntoNearbyStorage(ServerLevel level, Entity miner, BlockPos breakPos, BlockState blockState) {
		BlockEntity brokenEntity = level.getBlockEntity(breakPos);
		ItemStack tool = miner instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
		StorageTarget storage = findNearestChestOrBarrel(level, miner.position());
		if (storage == null) {
			level.destroyBlock(breakPos, true, miner);
			return;
		}

		List<ItemStack> drops = Block.getDrops(blockState, level, breakPos, brokenEntity, miner, tool);
		if (!level.destroyBlock(breakPos, false, miner)) {
			return;
		}

		for (ItemStack stack : drops) {
			ItemStack leftover = mergeInto(storage.container(), stack);
			if (!leftover.isEmpty()) {
				Block.popResource(level, breakPos, leftover);
			}
		}

		markStorageDirty(level, storage.pos(), storage.state());
	}

	private record StorageTarget(BlockPos pos, BlockState state, Container container) {}

	private static @Nullable StorageTarget findNearestChestOrBarrel(ServerLevel level, Vec3 minerPos) {
		BlockPos feet = BlockPos.containing(minerPos);
		BlockPos min = feet.offset(-STORAGE_SEARCH_RADIUS, -STORAGE_SEARCH_RADIUS, -STORAGE_SEARCH_RADIUS);
		BlockPos max = feet.offset(STORAGE_SEARCH_RADIUS, STORAGE_SEARCH_RADIUS, STORAGE_SEARCH_RADIUS);
		StorageTarget best = null;
		double bestDistSq = Double.MAX_VALUE;

		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (!level.isLoaded(pos)) {
				continue;
			}
			double distSq = minerPos.distanceToSqr(Vec3.atCenterOf(pos));
			if (distSq > STORAGE_SEARCH_RADIUS_SQ) {
				continue;
			}

			BlockState state = level.getBlockState(pos);
			Container container = containerFrom(level, pos, state);
			if (container == null) {
				continue;
			}

			if (distSq < bestDistSq) {
				bestDistSq = distSq;
				best = new StorageTarget(pos.immutable(), state, container);
			}
		}
		return best;
	}

	private static @Nullable Container containerFrom(ServerLevel level, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof BarrelBlock) {
			BlockEntity be = level.getBlockEntity(pos);
			return be instanceof Container container ? container : null;
		}
		if (state.getBlock() instanceof ChestBlock chestBlock) {
			return ChestBlock.getContainer(chestBlock, state, level, pos, false);
		}
		return null;
	}

	private static ItemStack mergeInto(Container dest, ItemStack incoming) {
		if (incoming.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = incoming.copy();
		for (int slot = 0; slot < dest.getContainerSize(); slot++) {
			if (!dest.canPlaceItem(slot, stack)) {
				continue;
			}
			ItemStack slotStack = dest.getItem(slot);
			if (slotStack.isEmpty() || !ItemStack.isSameItemSameComponents(slotStack, stack)) {
				continue;
			}
			int limit = Math.min(dest.getMaxStackSize(stack), stack.getMaxStackSize());
			int space = limit - slotStack.getCount();
			if (space <= 0) {
				continue;
			}
			int moved = Math.min(space, stack.getCount());
			slotStack.grow(moved);
			stack.shrink(moved);
			dest.setItem(slot, slotStack);
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}
		for (int slot = 0; slot < dest.getContainerSize(); slot++) {
			if (!dest.getItem(slot).isEmpty()) {
				continue;
			}
			if (!dest.canPlaceItem(slot, stack)) {
				continue;
			}
			dest.setItem(slot, stack.copy());
			return ItemStack.EMPTY;
		}
		return stack;
	}

	private static void markStorageDirty(ServerLevel level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity != null) {
			blockEntity.setChanged();
		}
		if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
			if (state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
				BlockPos connected = ChestBlock.getConnectedBlockPos(pos, state);
				BlockEntity other = level.getBlockEntity(connected);
				if (other != null) {
					other.setChanged();
				}
			}
		}
	}
}
