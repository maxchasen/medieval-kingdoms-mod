package com.medievalkingdoms.client.alliance;

import java.util.List;
import java.util.UUID;

import com.medievalkingdoms.features.alliance.AllianceRequestPayload;
import com.medievalkingdoms.features.alliance.AllyRow;
import com.medievalkingdoms.features.alliance.BreakAlliancePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AllianceTableScreen extends Screen {
	private final UUID kingdomId;
	private final List<AllyRow> allies;
	private EditBox targetNameField;

	public AllianceTableScreen(UUID kingdomId, List<AllyRow> allies) {
		super(Component.translatable("medieval_kingdoms.alliance.ui.title"));
		this.kingdomId = kingdomId;
		this.allies = allies;
	}

	@Override
	protected void init() {
		super.init();
		int cx = this.width / 2;
		int cy = this.height / 2;
		int y = cy - 70;
		for (AllyRow row : this.allies) {
			if (y > this.height - 95) {
				break;
			}
			final UUID otherId = row.kingdomId();
			this.addRenderableWidget(
					Button.builder(
							Component.translatable("medieval_kingdoms.alliance.ui.break_with", row.internalName()),
							b -> {
								ClientPlayNetworking.send(new BreakAlliancePayload(this.kingdomId, otherId));
								this.onClose();
							})
							.bounds(cx - 100, y, 200, 18)
							.build());
			y += 22;
		}
		this.targetNameField = new EditBox(this.font, cx - 100, cy - 22, 200, 20, Component.empty());
		this.targetNameField.setMaxLength(48);
		this.targetNameField.setHint(Component.translatable("medieval_kingdoms.alliance.ui.target_hint"));
		this.addRenderableWidget(this.targetNameField);
		this.setInitialFocus(this.targetNameField);
		this.addRenderableWidget(
				Button.builder(Component.translatable("medieval_kingdoms.alliance.ui.send_request"), b -> this.sendRequest())
						.bounds(cx - 60, cy + 10, 120, 20)
						.build());
	}

	private void sendRequest() {
		String name = this.targetNameField.getValue().trim();
		if (!name.isEmpty()) {
			ClientPlayNetworking.send(new AllianceRequestPayload(this.kingdomId, name));
		}
		this.onClose();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(null);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
