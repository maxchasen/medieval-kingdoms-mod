package com.medievalkingdoms.client.alliance;

import com.medievalkingdoms.features.alliance.SubmitKingdomNamePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** First-time naming for a placed kingdom sigil. */
public final class NameKingdomScreen extends Screen {
	private final BlockPos sigilPos;
	private EditBox nameField;

	public NameKingdomScreen(BlockPos sigilPos) {
		super(Component.translatable("medieval_kingdoms.kingdom.name.title"));
		this.sigilPos = sigilPos;
	}

	@Override
	protected void init() {
		super.init();
		int cx = this.width / 2;
		int cy = this.height / 2;
		this.nameField = new EditBox(this.font, cx - 100, cy - 12, 200, 20, Component.empty());
		this.nameField.setMaxLength(24);
		this.nameField.setHint(Component.translatable("medieval_kingdoms.kingdom.name.hint"));
		this.addRenderableWidget(this.nameField);
		this.setInitialFocus(this.nameField);
		this.addRenderableWidget(
				Button.builder(Component.translatable("medieval_kingdoms.kingdom.name.confirm"), b -> this.submit())
						.bounds(cx - 60, cy + 18, 120, 20)
						.build());
	}

	private void submit() {
		String name = this.nameField.getValue().trim();
		if (!name.isEmpty()) {
			ClientPlayNetworking.send(new SubmitKingdomNamePayload(this.sigilPos, name));
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
