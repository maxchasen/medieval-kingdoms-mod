package com.medievalkingdoms.client.alliance;

import com.medievalkingdoms.features.alliance.SubmitKingdomNamePayload;
import com.medievalkingdoms.features.realm.KingdomSigilPalette;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/** First-time naming + realm color for a placed kingdom sigil. */
public final class NameKingdomScreen extends Screen {
	private final BlockPos sigilPos;
	private EditBox nameField;
	private int selectedColorArgb = KingdomSigilPalette.DEFAULT_COLOR;

	public NameKingdomScreen(BlockPos sigilPos) {
		super(Component.translatable("medieval_kingdoms.kingdom.name.title"));
		this.sigilPos = sigilPos;
	}

	@Override
	protected void init() {
		super.init();
		int cx = this.width / 2;
		int cy = this.height / 2;
		this.nameField = new EditBox(this.font, cx - 100, cy - 36, 200, 20, Component.empty());
		this.nameField.setMaxLength(24);
		this.nameField.setHint(Component.translatable("medieval_kingdoms.kingdom.name.hint"));
		this.addRenderableWidget(this.nameField);
		this.setInitialFocus(this.nameField);

		int sw = 18;
		int cols = 8;
		int row0 = cy - 8;
		for (int i = 0; i < KingdomSigilPalette.COLORS.length; i++) {
			int argb = KingdomSigilPalette.COLORS[i];
			int bx = cx - (cols * (sw + 2)) / 2 + (i % cols) * (sw + 2);
			int by = row0 + (i / cols) * (sw + 2);
			MutableComponent swatch = Component.literal("\u25A0").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(argb & 0xFFFFFF)));
			final int pick = argb;
			this.addRenderableWidget(
					Button.builder(swatch, b -> this.selectedColorArgb = pick).bounds(bx, by, sw, sw).build());
		}

		this.addRenderableWidget(
				Button.builder(Component.translatable("medieval_kingdoms.kingdom.name.confirm"), b -> this.submit())
						.bounds(cx - 60, cy + 52, 120, 20)
						.build());
	}

	private void submit() {
		String name = this.nameField.getValue().trim();
		if (name.isEmpty()) {
			name = "Unnamed";
		}
		ClientPlayNetworking.send(new SubmitKingdomNamePayload(this.sigilPos, name, this.selectedColorArgb));
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

	@Override
	public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		graphics.drawString(
				this.font,
				Component.translatable("medieval_kingdoms.kingdom.name.color_label"),
				this.width / 2 - 100,
				this.height / 2 - 52,
				0xA0A0A0,
				false);
	}
}
