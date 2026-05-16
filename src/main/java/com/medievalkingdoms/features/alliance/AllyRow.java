package com.medievalkingdoms.features.alliance;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** One allied kingdom row for the alliance table screen. */
public record AllyRow(UUID kingdomId, String internalName) {
	public static final StreamCodec<RegistryFriendlyByteBuf, AllyRow> STREAM_CODEC =
			StreamCodec.composite(
					UUIDUtil.STREAM_CODEC,
					AllyRow::kingdomId,
					ByteBufCodecs.STRING_UTF8,
					AllyRow::internalName,
					AllyRow::new);

	public static StreamCodec<RegistryFriendlyByteBuf, java.util.List<AllyRow>> listCodec() {
		return AllyRow.STREAM_CODEC.apply(ByteBufCodecs.list(64));
	}
}
