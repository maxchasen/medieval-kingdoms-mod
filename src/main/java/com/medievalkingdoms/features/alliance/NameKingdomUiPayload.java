package com.medievalkingdoms.features.alliance;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Prompts the client to name a newly placed kingdom sigil. */
public record NameKingdomUiPayload(BlockPos sigilPos) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<NameKingdomUiPayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("name_kingdom_ui"));
	public static final StreamCodec<RegistryFriendlyByteBuf, NameKingdomUiPayload> STREAM_CODEC =
			StreamCodec.composite(BlockPos.STREAM_CODEC, NameKingdomUiPayload::sigilPos, NameKingdomUiPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
