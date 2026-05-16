package com.medievalkingdoms.features.alliance;

import java.util.UUID;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: remove a bilateral alliance edge (owner of myKingdomId only). */
public record BreakAlliancePayload(UUID myKingdomId, UUID otherKingdomId) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BreakAlliancePayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("break_alliance"));
	public static final StreamCodec<RegistryFriendlyByteBuf, BreakAlliancePayload> STREAM_CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC,
					BreakAlliancePayload::myKingdomId,
					UUIDUtil.STREAM_CODEC,
					BreakAlliancePayload::otherKingdomId,
					BreakAlliancePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
