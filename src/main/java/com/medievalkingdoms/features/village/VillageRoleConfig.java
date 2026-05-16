package com.medievalkingdoms.features.village;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

/**
 * Loads {@code config/medieval_kingdoms/village_roles.json} (Fabric config dir). Creates PRD-weighted defaults when missing.
 */
public final class VillageRoleConfig {
	private static final String VANILLA_KEY = "vanilla";

	private static volatile VillageRoleConfig instance = defaults();

	private final int vanillaWeight;
	private final LinkedHashMap<String, Integer> roleWeights;

	private VillageRoleConfig(int vanillaWeight, LinkedHashMap<String, Integer> roleWeights) {
		this.vanillaWeight = vanillaWeight;
		this.roleWeights = roleWeights;
	}

	/** Snapshot used before the first server {@link #reload}. */
	public static VillageRoleConfig get() {
		return instance;
	}

	public int vanillaWeight() {
		return vanillaWeight;
	}

	public Map<String, Integer> roleWeights() {
		return Map.copyOf(roleWeights);
	}

	/**
	 * Weighted roll across mod roles and the vanilla remainder. Empty means "stay vanilla" for downstream assignment (future milestones).
	 */
	public Optional<String> rollModRole(RandomSource random) {
		int modTotal = 0;
		for (int w : roleWeights.values()) {
			modTotal += Math.max(0, w);
		}
		int v = Math.max(0, vanillaWeight);
		int total = v + modTotal;
		if (total <= 0) {
			return Optional.empty();
		}
		int pick = random.nextInt(total);
		if (pick < v) {
			return Optional.empty();
		}
		int cursor = pick - v;
		for (var e : roleWeights.entrySet()) {
			int w = Math.max(0, e.getValue());
			if (w <= 0) {
				continue;
			}
			cursor -= w;
			if (cursor < 0) {
				return Optional.of(e.getKey());
			}
		}
		return Optional.empty();
	}

	public static synchronized void reload(Path villageRolesFile) throws IOException {
		instance = loadOrCreate(villageRolesFile);
	}

	private static VillageRoleConfig defaults() {
		return new VillageRoleConfig(5, defaultRoleWeights());
	}

	private static LinkedHashMap<String, Integer> defaultRoleWeights() {
		var m = new LinkedHashMap<String, Integer>();
		m.put("knight", 20);
		m.put("archer", 20);
		m.put("miner", 15);
		m.put("farmer", 12);
		m.put("guard", 10);
		m.put("smith", 10);
		m.put("cleric", 8);
		return m;
	}

	private static VillageRoleConfig loadOrCreate(Path file) throws IOException {
		Path dir = file.getParent();
		if (dir != null) {
			Files.createDirectories(dir);
		}
		if (!Files.isRegularFile(file)) {
			writeDefaults(file);
		}
		JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();

		int vanilla = GsonHelper.getAsInt(root, VANILLA_KEY, defaults().vanillaWeight);
		LinkedHashMap<String, Integer> weights = defaultRoleWeights();
		JsonObject weightObj = null;
		if (GsonHelper.isObjectNode(root, "weights")) {
			weightObj = GsonHelper.getAsJsonObject(root, "weights");
		}
		for (String key : weights.keySet()) {
			if (weightObj != null && weightObj.has(key)) {
				weights.put(key, Math.max(0, GsonHelper.getAsInt(weightObj, key, weights.get(key))));
			}
		}
		if (weightObj != null) {
			for (var e : weightObj.entrySet()) {
				String k = e.getKey();
				JsonElement el = e.getValue();
				if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
					weights.putIfAbsent(k, Math.max(0, el.getAsInt()));
				}
			}
		}
		return new VillageRoleConfig(Math.max(0, vanilla), weights);
	}

	private static void writeDefaults(Path file) throws IOException {
		JsonObject weights = new JsonObject();
		for (var e : defaultRoleWeights().entrySet()) {
			weights.addProperty(e.getKey(), e.getValue());
		}
		JsonObject root = new JsonObject();
		root.addProperty(VANILLA_KEY, defaults().vanillaWeight);
		root.add("weights", weights);
		var gson = new GsonBuilder().setPrettyPrinting().create();
		Files.writeString(file, gson.toJson(root), StandardCharsets.UTF_8);
	}
}
