package dev.xkmc.l2damagetracker.contents.damage;

import dev.xkmc.l2library.init.events.damage.DamageState;
import dev.xkmc.l2library.init.events.damage.DamageTypeRoot;
import dev.xkmc.l2library.init.events.damage.DamageTypeWrapper;
import dev.xkmc.l2library.init.events.damage.DamageWrapperTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public final class DamageTypeVariant implements DamageTypeWrapper {

	private final ResourceKey<DamageType> type;
	private final dev.xkmc.l2library.init.events.damage.DamageTypeRoot root;
	private final int key;
	private final TreeSet<DamageState> enabledStates;

	public DamageTypeVariant(String modid, dev.xkmc.l2library.init.events.damage.DamageTypeRoot root, int key, TreeSet<DamageState> set) {
		this.root = root;
		this.key = key;
		this.enabledStates = set;
		StringBuilder name = new StringBuilder(root.type().location().getPath());
		for (DamageState state : set) {
			name.append("-").append(state.getId().getPath());
		}
		this.type = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(modid, name.toString()));
	}

	public ResourceKey<DamageType> type() {
		return type;
	}

	@Override
	public boolean validState(DamageState state) {
		return root.validState(state);
	}

	@Override
	public boolean isEnabled(DamageState state) {
		return root.isEnabled(key, state);
	}

	@Nullable
	@Override
	public DamageTypeWrapper enable(DamageState state) {
		return root.get(key, state);
	}

	@Override
	public DamageTypeWrapper toRoot() {
		return root;
	}

	public DamageTypeRoot root() {
		return root;
	}

	@Override
	public void gen(DamageWrapperTagProvider gen, HolderLookup.Provider pvd) {
		TreeSet<TagKey<DamageType>> tags = new TreeSet<>(Comparator.comparing(TagKey::location));
		tags.addAll(root.inherent);
		for (DamageState state : root.states) {
			if (isEnabled(state)) {
				state.gatherTags(tags::add);
			}
		}
		for (TagKey<DamageType> tag : tags) {
			gen.tag(tag).add(type);
		}
	}

	@Override
	public DamageType getObject() {
		return root().sup.apply(this);
	}

	@Override
	public Set<DamageState> states() {
		return enabledStates;
	}
}
