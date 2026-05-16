package com.medievalkingdoms.features.realm;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

/**
 * Persistent kingdom fields stored in overworld {@link KingdomWorldData}.
 */
public final class KingdomEntry {
	public static final Codec<KingdomEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("internal_name").forGetter(KingdomEntry::internalName),
			UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(e -> Optional.ofNullable(e.owner)),
			UUIDUtil.CODEC_SET.optionalFieldOf("allied_kingdoms", Set.of()).forGetter(e -> Set.copyOf(e.alliedKingdomIds))
	).apply(instance, KingdomEntry::fromCodec));

	final String internalName;
	/** Owning player, when set at creation from a {@link net.minecraft.server.level.ServerPlayer}. */
	UUID owner;
	final Set<UUID> alliedKingdomIds;

	public KingdomEntry(String internalName, UUID owner) {
		this.internalName = internalName;
		this.owner = owner;
		this.alliedKingdomIds = new HashSet<>();
	}

	private static KingdomEntry fromCodec(String name, Optional<UUID> owner, Set<UUID> allies) {
		KingdomEntry e = new KingdomEntry(name, owner.orElse(null));
		e.alliedKingdomIds.addAll(allies);
		return e;
	}

	public String internalName() {
		return this.internalName;
	}

	public UUID owner() {
		return this.owner;
	}

	public Set<UUID> alliedKingdomIds() {
		return this.alliedKingdomIds;
	}
}
