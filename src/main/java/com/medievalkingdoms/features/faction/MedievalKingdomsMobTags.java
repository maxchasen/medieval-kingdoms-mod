package com.medievalkingdoms.features.faction;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;

/**
 * Persistent kingdom id under entity {@link DataComponents#CUSTOM_DATA} ({@code data} tag),
 * matching vanilla entity component storage for MC 1.21+.
 */
public final class MedievalKingdomsMobTags {
	/** Root compound key storing kingdom UUID as {@link UUIDUtil#uuidToIntArray(UUID)}. */
	public static final String KINGDOM_ID = "mk_kingdom";
	/** Village / baby role id from weighted roll (e.g. {@code knight}, {@code archer}). */
	public static final String VILLAGE_ROLE_ID = "mk_role";
	public static final String ECONOMY_GOALS_REGISTERED = "mk_eco_goals";

	private MedievalKingdomsMobTags() {
	}

	public static Optional<UUID> readKingdomId(Entity entity) {
		var root = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		Optional<int[]> ints = root.getIntArray(KINGDOM_ID);
		if (ints.isEmpty()) {
			return Optional.empty();
		}
		int[] arr = ints.get();
		if (arr.length != 4) {
			return Optional.empty();
		}
		return Optional.of(UUIDUtil.uuidFromIntArray(arr));
	}

	public static void writeKingdomId(Entity entity, UUID kingdomId) {
		CustomData next = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.update(tag -> tag.putIntArray(KINGDOM_ID, UUIDUtil.uuidToIntArray(kingdomId)));
		entity.setComponent(DataComponents.CUSTOM_DATA, next.isEmpty() ? CustomData.EMPTY : next);
	}

	public static void removeKingdomId(Entity entity) {
		CustomData next = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.update(tag -> tag.remove(KINGDOM_ID));
		entity.setComponent(DataComponents.CUSTOM_DATA, next.isEmpty() ? CustomData.EMPTY : next);
	}

	public static Optional<String> readVillageRole(Entity entity) {
		var root = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return root.getString(VILLAGE_ROLE_ID).filter(s -> !s.isEmpty());
	}

	public static void writeVillageRole(Entity entity, String roleId) {
		CustomData next = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.update(tag -> tag.putString(VILLAGE_ROLE_ID, roleId));
		entity.setComponent(DataComponents.CUSTOM_DATA, next.isEmpty() ? CustomData.EMPTY : next);
	}

	public static void removeVillageRole(Entity entity) {
		CustomData next = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.update(tag -> tag.remove(VILLAGE_ROLE_ID));
		entity.setComponent(DataComponents.CUSTOM_DATA, next.isEmpty() ? CustomData.EMPTY : next);
	}

	public static boolean hasEconomyGoalsRegistered(Entity entity) {
		return entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.copyTag()
				.getBoolean(ECONOMY_GOALS_REGISTERED)
				.orElse(false);
	}

	public static void markEconomyGoalsRegistered(Entity entity) {
		CustomData next = entity.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
				.update(tag -> tag.putBoolean(ECONOMY_GOALS_REGISTERED, true));
		entity.setComponent(DataComponents.CUSTOM_DATA, next.isEmpty() ? CustomData.EMPTY : next);
	}
}
