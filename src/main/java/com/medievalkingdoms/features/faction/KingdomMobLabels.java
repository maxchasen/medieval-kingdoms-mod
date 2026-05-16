package com.medievalkingdoms.features.faction;

import java.util.Optional;
import java.util.UUID;

import com.medievalkingdoms.features.realm.KingdomEntry;
import com.medievalkingdoms.features.realm.KingdomWorldData;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/** Nametags: colored square from realm palette + short role label when tagged with mk_kingdom. */
public final class KingdomMobLabels {
	private KingdomMobLabels() {
	}

	public static void applyRoleName(LivingEntity entity, ServerLevel level, String roleEnglish) {
		Optional<UUID> kid = MedievalKingdomsMobTags.readKingdomId(entity);
		if (kid.isEmpty()) {
			entity.setCustomName(Component.literal(roleEnglish));
			entity.setCustomNameVisible(true);
			return;
		}
		Optional<KingdomEntry> entry = KingdomWorldData.get(level).getKingdom(kid.get());
		if (entry.isEmpty()) {
			entity.setCustomName(Component.literal(roleEnglish));
			entity.setCustomNameVisible(true);
			return;
		}
		int rgb = entry.get().labelColor() & 0xFFFFFF;
		MutableComponent label = Component.literal("\u25A0 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
		label.append(Component.literal(roleEnglish));
		entity.setCustomName(label);
		entity.setCustomNameVisible(true);
	}
}
