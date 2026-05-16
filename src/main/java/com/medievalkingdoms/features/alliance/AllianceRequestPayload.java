package com.medievalkingdoms.features.alliance;

import java.util.UUID;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Serverbound: kingdom owner requests alliance with a target identified by internal name. */
public record AllianceRequestPayload(UUID proposerKingdomId, String targetNameString) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AllianceRequestPayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("alliance_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AllianceRequestPayload> STREAM_CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC,
					AllianceRequestPayload::proposerKingdomId,
					ByteBufCodecs.STRING_UTF8,
					AllianceRequestPayload::targetNameString,
					AllianceRequestPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
