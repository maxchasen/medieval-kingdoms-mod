package com.medievalkingdoms.features.alliance;

import java.util.UUID;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Serverbound context is not needed: server opens UI for sigil owner with anchored kingdom id. */
public record AllianceOpenUiPayload(BlockPos sigilPos, UUID kingdomId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AllianceOpenUiPayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("alliance_open_ui"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AllianceOpenUiPayload> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					AllianceOpenUiPayload::sigilPos,
					UUIDUtil.STREAM_CODEC,
					AllianceOpenUiPayload::kingdomId,
					AllianceOpenUiPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
