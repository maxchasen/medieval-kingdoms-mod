package com.medievalkingdoms.features.alliance;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Clientbound: toast/chat-style notification (e.g. incoming alliance request). */
public record AllianceNotificationPayload(String messageKey, String kingdomName) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AllianceNotificationPayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("alliance_notification"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AllianceNotificationPayload> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8,
					AllianceNotificationPayload::messageKey,
					ByteBufCodecs.STRING_UTF8,
					AllianceNotificationPayload::kingdomName,
					AllianceNotificationPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
