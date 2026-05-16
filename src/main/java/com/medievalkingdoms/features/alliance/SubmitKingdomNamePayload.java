package com.medievalkingdoms.features.alliance;

import com.medievalkingdoms.MedievalKingdomsMod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** C2S: finalize kingdom name for an unnamed sigil (owner only). */
public record SubmitKingdomNamePayload(BlockPos sigilPos, String kingdomName) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SubmitKingdomNamePayload> TYPE =
			new CustomPacketPayload.Type<>(MedievalKingdomsMod.id("submit_kingdom_name"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SubmitKingdomNamePayload> STREAM_CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC,
					SubmitKingdomNamePayload::sigilPos,
					ByteBufCodecs.STRING_UTF8,
					SubmitKingdomNamePayload::kingdomName,
					SubmitKingdomNamePayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
