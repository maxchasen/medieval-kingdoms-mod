package com.medievalkingdoms.features.village;

import java.util.List;

/** Mod roles that replace villagers with a custom workforce entity. */
public final class VillageRoleCatalog {
	public static final List<String> ASSIGNABLE_ROLES =
			List.of("knight", "archer", "miner", "smith", "armorsmith", "fletcher");

	private VillageRoleCatalog() {
	}

	public static boolean isAssignable(String roleId) {
		return ASSIGNABLE_ROLES.contains(roleId);
	}
}
