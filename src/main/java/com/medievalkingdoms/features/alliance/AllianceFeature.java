package com.medievalkingdoms.features.alliance;

import java.util.Map;
import java.util.UUID;

import com.medievalkingdoms.features.realm.KingdomEntry;
import com.medievalkingdoms.features.realm.KingdomWorldData;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Alliance requests accept/decline and saved alliance graph (PRD §6.4). */
public final class AllianceFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(AllianceRequestPayload.TYPE, AllianceRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(AllianceNotificationPayload.TYPE, AllianceNotificationPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(AllianceRequestPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ServerLevel level = (ServerLevel) player.level();
			KingdomWorldData data = KingdomWorldData.get(level);
			UUID proposerId = payload.proposerKingdomId();
			KingdomEntry proposer = data.getKingdom(proposerId).orElse(null);
			if (proposer == null || proposer.owner() == null || !proposer.owner().equals(player.getUUID())) {
				return;
			}
			String targetName = payload.targetNameString();
			UUID targetId = resolveKingdomIdByInternalName(data.getKingdomsView(), targetName);
			if (targetId == null || targetId.equals(proposerId)) {
				return;
			}
			if (!data.proposeAlliance(proposerId, targetId)) {
				return;
			}
			KingdomEntry target = data.getKingdom(targetId).orElse(null);
			if (target == null || target.owner() == null) {
				return;
			}
			ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(target.owner());
			if (targetPlayer != null) {
				ServerPlayNetworking.send(targetPlayer, new AllianceNotificationPayload(
						"medieval_kingdoms.alliance.request_received",
						proposer.internalName()));
			}
		});
	}

	private static UUID resolveKingdomIdByInternalName(Map<UUID, KingdomEntry> kingdoms, String internalName) {
		for (Map.Entry<UUID, KingdomEntry> e : kingdoms.entrySet()) {
			if (e.getValue().internalName().equals(internalName)) {
				return e.getKey();
			}
		}
		return null;
	}
}
