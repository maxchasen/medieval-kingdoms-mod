package com.medievalkingdoms.features.economy;

import com.medievalkingdoms.features.faction.MedievalKingdomsMobTags;
import com.medievalkingdoms.mixin.VillagerProfessionGoalsMixin;

import net.minecraft.world.entity.npc.villager.Villager;

/** Economy villager goals (injected via {@link VillagerProfessionGoalsMixin}). */
public final class VillagerProfessionGoals {
	private VillagerProfessionGoals() {
	}

	public static void ensureRegistered(Villager villager) {
		if (!MedievalKingdomsMobTags.hasEconomyGoalsRegistered(villager)) {
			((VillagerProfessionGoalsMixin) (Object) villager).addEconomyGoals();
		}
	}
}
