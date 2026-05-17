package com.medievalkingdoms.features.faction;

import java.util.Optional;
import java.util.UUID;

import com.medievalkingdoms.entity.MinerEntity;
import com.medievalkingdoms.features.combat.entity.ArcherEntity;
import com.medievalkingdoms.features.combat.entity.KnightEntity;
import com.medievalkingdoms.features.realm.KingdomEntry;
import com.medievalkingdoms.features.realm.KingdomWorldData;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/** Nametags: kingdom color square on the left + short role label when tagged with mk_kingdom. */
public final class KingdomMobLabels {
	private KingdomMobLabels() {
	}

	public static void applyRoleName(LivingEntity entity, ServerLevel level, String roleEnglish) {
		applyRoleName(entity, level, Component.literal(roleEnglish));
	}

	public static void applyRoleName(LivingEntity entity, ServerLevel level, Component role) {
		Optional<UUID> kid = MedievalKingdomsMobTags.readKingdomId(entity);
		if (kid.isEmpty()) {
			entity.setCustomName(role.copy());
			entity.setCustomNameVisible(true);
			return;
		}
		Optional<KingdomEntry> entry = KingdomWorldData.get(level).getKingdom(kid.get());
		if (entry.isEmpty()) {
			entity.setCustomName(role.copy());
			entity.setCustomNameVisible(true);
			return;
		}
		entity.setCustomName(buildKingdomNametag(entry.get(), role));
		entity.setCustomNameVisible(true);
	}

	/** Re-applies the colored square + role for any entity already in a kingdom (keeps labels in sync). */
	public static void refreshForEntity(LivingEntity entity, ServerLevel level) {
		applyRoleName(entity, level, resolveRoleComponent(entity));
	}

	public static boolean isKingdomWorkforce(LivingEntity entity) {
		return entity instanceof Villager
				|| entity instanceof MinerEntity
				|| entity instanceof KnightEntity
				|| entity instanceof ArcherEntity;
	}

	private static Component resolveRoleComponent(LivingEntity entity) {
		if (entity instanceof Villager villager) {
			return villagerProfessionName(villager);
		}
		if (entity instanceof KnightEntity) {
			return Component.literal("Knight");
		}
		if (entity instanceof ArcherEntity) {
			return Component.literal("Archer");
		}
		if (entity instanceof MinerEntity) {
			return Component.literal("Miner");
		}
		return entity.getType().getDescription();
	}

	private static Component villagerProfessionName(Villager villager) {
		VillagerData data = villager.getVillagerData();
		if (data.profession().is(VillagerProfession.NONE)) {
			return Component.literal("Villager");
		}
		return data.profession().value().name().copy();
	}

	private static MutableComponent buildKingdomNametag(KingdomEntry entry, Component role) {
		int rgb = entry.labelColor() & 0xFFFFFF;
		MutableComponent label = Component.literal("\u25A0 ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));
		label.append(role);
		return label;
	}
}
