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

	private static final Codec<Map<UUID, UUID>> PENDING_ALLIANCES_CODEC =
			Codec.unboundedMap(UUIDUtil.STRING_CODEC, UUIDUtil.STRING_CODEC);

	public static final Codec<KingdomWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KINGDOM_MAP_CODEC.optionalFieldOf("kingdoms", Map.of()).forGetter(d -> Map.copyOf(d.kingdoms)),
			PENDING_ALLIANCES_CODEC.optionalFieldOf("pending_alliances", Map.of())
					.forGetter(d -> Map.copyOf(d.pendingAllianceFromTo))
	).apply(instance, KingdomWorldData::fromCodecMaps));

	private static final SavedDataType<KingdomWorldData> TYPE = new SavedDataType<>(
			DATA_ID,
			KingdomWorldData::new,
			CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Map<UUID, KingdomEntry> kingdoms;
	private final Map<UUID, UUID> pendingAllianceFromTo;

	public KingdomWorldData() {
		this.kingdoms = new HashMap<>();
		this.pendingAllianceFromTo = new HashMap<>();
	}

	private KingdomWorldData(HashMap<UUID, KingdomEntry> kingdoms, HashMap<UUID, UUID> pendingAllianceFromTo) {
		this.kingdoms = kingdoms;
		this.pendingAllianceFromTo = pendingAllianceFromTo;
	}

	private static KingdomWorldData fromCodecMaps(Map<UUID, KingdomEntry> map, Map<UUID, UUID> pending) {
		return new KingdomWorldData(new HashMap<>(map), new HashMap<>(pending));
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

	public boolean isKingdomOwner(UUID playerId) {
		for (KingdomEntry e : this.kingdoms.values()) {
			if (playerId.equals(e.owner())) {
				return true;
			}
		}
		return false;
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
		this.pendingAllianceFromTo.entrySet()
				.removeIf(e -> e.getKey().equals(kingdomId) || e.getValue().equals(kingdomId));
		if (this.kingdoms.remove(kingdomId) != null) {
			this.setDirty();
		}
	}

	public boolean proposeAlliance(UUID from, UUID to) {
		if (from.equals(to) || !this.kingdoms.containsKey(from) || !this.kingdoms.containsKey(to)) {
			return false;
		}
		this.pendingAllianceFromTo.put(from, to);
		this.setDirty();
		return true;
	}

	public boolean acceptAlliance(UUID accepterKingdom, UUID proposerKingdom) {
		UUID pendingTarget = this.pendingAllianceFromTo.get(proposerKingdom);
		if (pendingTarget == null || !pendingTarget.equals(accepterKingdom)) {
			return false;
		}
		this.pendingAllianceFromTo.remove(proposerKingdom);
		this.addBidirectionalAlliance(proposerKingdom, accepterKingdom);
		return true;
	}

	public boolean declineAlliance(UUID targetKingdom, UUID proposerKingdom) {
		UUID pendingTarget = this.pendingAllianceFromTo.get(proposerKingdom);
		if (pendingTarget == null || !pendingTarget.equals(targetKingdom)) {
			return false;
		}
		this.pendingAllianceFromTo.remove(proposerKingdom);
		this.setDirty();
		return true;
	}

	public void addBidirectionalAlliance(UUID a, UUID b) {
		if (a.equals(b)) {
			return;
		}
		KingdomEntry ea = this.kingdoms.get(a);
		KingdomEntry eb = this.kingdoms.get(b);
		if (ea == null || eb == null) {
			return;
		}
		ea.addAlly(b);
		eb.addAlly(a);
		this.setDirty();
	}
}
