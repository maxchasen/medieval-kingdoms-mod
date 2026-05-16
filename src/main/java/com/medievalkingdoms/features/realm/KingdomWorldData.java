package com.medievalkingdoms.features.realm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.medievalkingdoms.MedievalKingdomsMod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Overworld kingdom registry (PRD §4.2). Always use {@link #get(ServerLevel)} /
 * {@link #get(MinecraftServer)} so data lives on the overworld dimension storage.
 */
public final class KingdomWorldData extends SavedData {
	public static final String DATA_ID = MedievalKingdomsMod.MOD_ID + ":kingdom_world_data";

	private static final Codec<Map<UUID, KingdomEntry>> KINGDOM_MAP_CODEC =
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, KingdomEntry.CODEC);

	public static final Codec<KingdomWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KINGDOM_MAP_CODEC.optionalFieldOf("kingdoms", Map.of()).forGetter(d -> Map.copyOf(d.kingdoms))
	).apply(instance, KingdomWorldData::fromCodecMap));

	private static final SavedDataType<KingdomWorldData> TYPE = new SavedDataType<>(
			DATA_ID,
			KingdomWorldData::new,
			CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Map<UUID, KingdomEntry> kingdoms = new HashMap<>();

	public KingdomWorldData() {
	}

	private KingdomWorldData(Map<UUID, KingdomEntry> fromDisk) {
		this.kingdoms.putAll(fromDisk);
	}

	private static KingdomWorldData fromCodecMap(Map<UUID, KingdomEntry> map) {
		return new KingdomWorldData(new HashMap<>(map));
	}

	public static KingdomWorldData get(ServerLevel level) {
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public static KingdomWorldData get(MinecraftServer server) {
		return get(server.getLevel(Level.OVERWORLD));
	}

	/** Prime codec + disk path as soon as the server exists (Fabric lifecycle hook). */
	public static void ensureLoaded(MinecraftServer server) {
		get(server);
	}

	public Map<UUID, KingdomEntry> getKingdomsView() {
		return Collections.unmodifiableMap(this.kingdoms);
	}

	public Optional<KingdomEntry> getKingdom(UUID id) {
		return Optional.ofNullable(this.kingdoms.get(id));
	}

	/**
	 * Creates a kingdom with a new id. Display/internal name is already normalized by the caller.
	 */
	public UUID createKingdom(String internalName, UUID owner) {
		UUID id = UUID.randomUUID();
		this.kingdoms.put(id, new KingdomEntry(internalName, owner));
		this.setDirty();
		return id;
	}

	public void removeKingdom(@Nullable UUID kingdomId) {
		if (kingdomId == null) {
			return;
		}
		if (this.kingdoms.remove(kingdomId) != null) {
			this.setDirty();
		}
	}
}
