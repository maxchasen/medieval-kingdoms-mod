package com.medievalkingdoms.client;

import com.medievalkingdoms.features.alliance.AllianceNotificationPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

/**
 * Client receivers for alliance notifications.
 * <p>
 * Sending {@link com.medievalkingdoms.features.alliance.AllianceRequestPayload} can be wired later
 * (scroll GUI or an optional {@code /trigger}-style stub); this entrypoint only handles inbound
 * {@link AllianceNotificationPayload}.
 */
public final class AllianceClient implements ClientModInitializer {
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
	}
}
