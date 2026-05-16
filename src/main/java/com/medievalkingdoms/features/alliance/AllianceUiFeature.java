package com.medievalkingdoms.features.alliance;

import com.medievalkingdoms.features.realm.KingdomSigilBlock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/** Sigil alliance table screen open + packet wiring (PRD §6–7). */
public final class AllianceUiFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(AllianceOpenUiPayload.TYPE, AllianceOpenUiPayload.STREAM_CODEC);
		UseBlockCallback.EVENT.register(
				(player, world, hand, hitResult) -> KingdomSigilBlock.allianceTableUse(world, hitResult.getBlockPos(), player, hand));
	}
}
