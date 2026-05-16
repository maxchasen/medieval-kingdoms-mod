package com.medievalkingdoms.client;

import com.medievalkingdoms.client.alliance.AllianceTableScreen;
import com.medievalkingdoms.client.alliance.NameKingdomScreen;
import com.medievalkingdoms.features.alliance.AllianceNotificationPayload;
import com.medievalkingdoms.features.alliance.AllianceOpenUiPayload;
import com.medievalkingdoms.features.alliance.NameKingdomUiPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/** Client UI + receivers for alliance flow (notifications + sigil table). */
public final class AllianceUiClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(AllianceNotificationPayload.TYPE, (payload, context) -> {
			Minecraft client = context.client();
			SystemToast.add(
					client.getToastManager(),
					SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
					Component.translatable("medieval_kingdoms.alliance.toast.title"),
					Component.translatable(payload.messageKey(), payload.kingdomName()));
		});
		ClientPlayNetworking.registerGlobalReceiver(AllianceOpenUiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> Minecraft.getInstance()
					.setScreen(new AllianceTableScreen(payload.kingdomId(), payload.allies())));
		});
		ClientPlayNetworking.registerGlobalReceiver(NameKingdomUiPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> Minecraft.getInstance().setScreen(new NameKingdomScreen(payload.sigilPos())));
		});
	}
}
