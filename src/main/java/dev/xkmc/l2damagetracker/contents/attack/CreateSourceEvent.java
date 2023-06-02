package dev.xkmc.l2damagetracker.contents.attack;

import dev.xkmc.l2damagetracker.contents.damage.DamageState;
import dev.xkmc.l2damagetracker.contents.damage.DamageTypeRoot;
import dev.xkmc.l2damagetracker.contents.damage.DamageTypeWrapper;
import dev.xkmc.l2library.init.L2Library;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class CreateSourceEvent {

	private final Registry<DamageType> registry;
	private final ResourceKey<DamageType> original;
	private final LivingEntity attacker;
	private final Entity direct;

	@Nullable
	private PlayerAttackCache playerAttackCache = null;

	@Nullable
	private DamageTypeWrapper result;

	public CreateSourceEvent(Registry<DamageType> registry, ResourceKey<DamageType> mobAttack, LivingEntity entity, @Nullable Entity direct) {
		this.registry = registry;
		this.original = mobAttack;
		this.attacker = entity;
		this.direct = direct;
		this.result = DamageTypeRoot.ROOTS.get(original);
	}

	public ResourceKey<DamageType> getOriginal() {
		return original;
	}

	public LivingEntity getAttacker() {
		return attacker;
	}

	@Nullable
	public Entity getDirect() {
		return direct;
	}

	@Nullable
	public DamageTypeWrapper getResult() {
		return result;
	}

	/**
	 * use enable instead
	 */
	@Deprecated
	public void setResult(DamageTypeWrapper result) {
		this.result = result;
	}

	@Nullable
	public PlayerAttackCache getPlayerAttackCache() {
		return playerAttackCache;
	}

	public void setPlayerAttackCache(PlayerAttackCache playerAttackCache) {
		this.playerAttackCache = playerAttackCache;
	}

	public Registry<DamageType> getRegistry() {
		return registry;
	}

	public void enable(DamageState state) {
		if (result == null) {
			L2Library.LOGGER.warn("DamageType " + original.location() + " is not mutable");
			return;
		}
		if (!result.validState(state)) {
			L2Library.LOGGER.warn("DamageType " + result.type().location() + " does not contain state " + state.getId());
			return;
		}
		if (result.isEnabled(state)) return;
		DamageTypeWrapper next = result.enable(state);
		if (next == null || !registry.containsKey(next.type())) {
			boolean all = true;
			boolean covered = false;
			for (DamageState old : result.states()) {
				all &= state.overrides(old);
				covered |= old.overrides(state);
			}
			if (all) {
				next = result.toRoot().enable(state);
			} else if (covered) {
				next = result;
			} else {
				L2Library.LOGGER.warn("DamageType " + result.type().location() + " cannot enable state " + state.getId());
				return;
			}
		}
		if (next != null) {
			result = next;
		}
	}


}
