package com.medievalkingdoms.features.realm;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class KingdomSigilBlockEntity extends BlockEntity {
	@Nullable
	private UUID kingdomId;
	private String kingdomName = "Unnamed";
	@Nullable
	private UUID owner;

	public KingdomSigilBlockEntity(BlockPos pos, BlockState state) {
		super(RealmFeature.KINGDOM_SIGIL_BE_TYPE, pos, state);
	}

	public void setKingdomData(UUID kingdomId, String kingdomName, @Nullable UUID owner) {
		this.kingdomId = kingdomId;
		this.kingdomName = kingdomName;
		this.owner = owner;
		this.setChanged();
	}

	/** Called when the sigil is placed: kingdom id is assigned after the owner names it via UI. */
	public void setPlacedPendingName(@Nullable UUID owner) {
		this.kingdomId = null;
		this.kingdomName = "";
		this.owner = owner;
		this.setChanged();
	}

	@Nullable
	public UUID getKingdomId() {
		return this.kingdomId;
	}

	public String getKingdomName() {
		return this.kingdomName;
	}

	@Nullable
	public UUID getOwner() {
		return this.owner;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.kingdomId != null) {
			output.store("KingdomId", UUIDUtil.CODEC, this.kingdomId);
		}
		output.putString("KingdomName", this.kingdomName);
		output.storeNullable("Owner", UUIDUtil.CODEC, this.owner);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.kingdomId = input.read("KingdomId", UUIDUtil.CODEC).orElse(null);
		this.kingdomName = input.getStringOr("KingdomName", "Unnamed");
		this.owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
	}
}
