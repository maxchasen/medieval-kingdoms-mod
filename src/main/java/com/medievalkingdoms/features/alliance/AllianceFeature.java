package com.medievalkingdoms.features.alliance;

import java.util.Map;
import java.util.UUID;

import com.medievalkingdoms.features.realm.KingdomEntry;
import com.medievalkingdoms.features.realm.KingdomSigilBlockEntity;
import com.medievalkingdoms.features.realm.KingdomWorldData;
import com.medievalkingdoms.features.faction.VillagerKingdom;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Alliance requests accept/decline and saved alliance graph (PRD §6.4). */
public final class AllianceFeature implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(AllianceRequestPayload.TYPE, AllianceRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(SubmitKingdomNamePayload.TYPE, SubmitKingdomNamePayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(BreakAlliancePayload.TYPE, BreakAlliancePayload.STREAM_CODEC);
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

		ServerPlayNetworking.registerGlobalReceiver(SubmitKingdomNamePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ServerLevel level = (ServerLevel) player.level();
			BlockPos pos = payload.sigilPos();
			BlockEntity rawBe = level.getBlockEntity(pos);
			if (!(rawBe instanceof KingdomSigilBlockEntity sigil)) {
				return;
			}
			if (sigil.getOwner() == null || !sigil.getOwner().equals(player.getUUID())) {
				return;
			}
			if (sigil.getKingdomId() != null) {
				return;
			}
			String name = payload.kingdomName().trim();
			if (name.isEmpty()) {
				name = "Unnamed";
			}
			if (name.length() > 24) {
				name = name.substring(0, 24);
			}
			UUID kingdomId = KingdomWorldData.get(level).createKingdom(name, player.getUUID());
			sigil.setKingdomData(kingdomId, name, player.getUUID());
			VillagerKingdom.assignToKingdom(
					level,
					pos,
					kingdomId,
					VillagerKingdom.DEFAULT_HORIZONTAL_RADIUS,
					VillagerKingdom.DEFAULT_VERTICAL_RADIUS);
		});

		ServerPlayNetworking.registerGlobalReceiver(BreakAlliancePayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ServerLevel level = (ServerLevel) player.level();
			KingdomWorldData data = KingdomWorldData.get(level);
			KingdomEntry self = data.getKingdom(payload.myKingdomId()).orElse(null);
			if (self == null || self.owner() == null || !self.owner().equals(player.getUUID())) {
				return;
			}
			data.removeBidirectionalAlliance(payload.myKingdomId(), payload.otherKingdomId());
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
