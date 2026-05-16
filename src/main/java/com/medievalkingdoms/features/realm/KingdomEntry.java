package com.medievalkingdoms.features.realm;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

/**
 * Persistent kingdom fields stored on overworld {@link KingdomWorldData}.
 */
public final class KingdomEntry {
	public static final Codec<KingdomEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("internal_name").forGetter(KingdomEntry::internalName),
			UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(e -> Optional.ofNullable(e.owner)),
			UUIDUtil.CODEC_SET.optionalFieldOf("allied_kingdoms", Set.of()).forGetter(e -> Set.copyOf(e.alliedKingdomIds)),
			Codec.INT.optionalFieldOf("label_color", KingdomSigilPalette.DEFAULT_COLOR).forGetter(KingdomEntry::labelColor)
	).apply(instance, KingdomEntry::fromCodec));

	final String internalName;
	/** Owning player, when set at creation from a {@link net.minecraft.server.level.ServerPlayer}. */
	UUID owner;
	final Set<UUID> alliedKingdomIds;
	final int labelColor;

	public KingdomEntry(String internalName, UUID owner, int labelColor) {
		this.internalName = internalName;
		this.owner = owner;
		this.alliedKingdomIds = new HashSet<>();
		this.labelColor = labelColor;
	}

	private static KingdomEntry fromCodec(String name, Optional<UUID> owner, Set<UUID> allies, int labelColor) {
		KingdomEntry e = new KingdomEntry(name, owner.orElse(null), labelColor);
		e.alliedKingdomIds.addAll(allies);
		return e;
	}

	public String internalName() {
		return this.internalName;
	}

	public UUID owner() {
		return this.owner;
	}

	public int labelColor() {
		return this.labelColor;
	}

	public Set<UUID> alliedKingdomIds() {
		return this.alliedKingdomIds;
	}

	void addAlly(UUID kingdomId) {
		if (kingdomId != null) {
			this.alliedKingdomIds.add(kingdomId);
		}
	}

	void removeAlly(UUID kingdomId) {
		if (kingdomId != null) {
			this.alliedKingdomIds.remove(kingdomId);
		}
	}
}
