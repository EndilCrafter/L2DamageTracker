package dev.xkmc.l2damagetracker.contents.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public class AttackCache {

	int recursive = 0;

	private boolean damageFrozen = true;
	private boolean damageMaximized = false;

	private Stage stage = Stage.PREINIT;
	private PlayerAttackCache player;
	private LivingAttackEvent attack;
	private LivingHurtEvent hurt;
	private LivingDamageEvent damage;

	private LivingEntity target;
	private LivingEntity attacker;

	private ItemStack weapon = ItemStack.EMPTY;

	private float damage_pre;
	private float damage_modified;
	private float damage_dealt;

	private final List<DamageModifier> modifierHurt = new ArrayList<>();
	private final List<DamageModifier> modifierDealt = new ArrayList<>();

	void pushAttackPre(LivingAttackEvent event) {
		stage = Stage.HURT_PRE;
		attack = event;
		target = attack.getEntity();
		damage_pre = event.getAmount();
		AttackEventHandler.getListeners().forEach(e -> e.onAttack(this, weapon));
	}

	void pushAttackPost(LivingAttackEvent event) {
		stage = Stage.HURT_POST;
	}

	void pushHurtPre(LivingHurtEvent event) {
		stage = Stage.ACTUALLY_HURT_PRE;
		hurt = event;
		damageFrozen = false;
		AttackEventHandler.getListeners().forEach(e -> e.onHurt(this, weapon));
		damageFrozen = true;
		damage_modified = event.getAmount();
		Comparator<DamageModifier> comp = Comparator.comparingInt(e -> e.order().ordinal());
		comp = comp.thenComparingInt(DamageModifier::priority);
		modifierHurt.sort(comp);
		for (DamageModifier mod : modifierHurt) {
			damage_modified = mod.modify(damage_modified);
		}
		if (damage_modified != event.getAmount()) {
			event.setAmount(damage_modified);
		}
		damageMaximized = true;
		AttackEventHandler.getListeners().forEach(e -> e.onHurtMaximized(this, weapon));
	}

	void pushHurtPost(LivingHurtEvent event) {
		stage = Stage.ACTUALLY_HURT_POST;
	}

	void pushDamagePre(LivingDamageEvent event) {
		stage = Stage.DAMAGE_PRE;
		damage = event;
		AttackEventHandler.getListeners().forEach(e -> e.onDamage(this, weapon));
		damage_dealt = event.getAmount();
		Comparator<DamageModifier> comp = Comparator.comparingInt(e -> e.order().ordinal());
		comp = comp.thenComparingInt(DamageModifier::priority);
		modifierDealt.sort(comp);
		for (DamageModifier mod : modifierDealt) {
			damage_dealt = mod.modify(damage_dealt);
		}
		if (damage_dealt != event.getAmount()) {
			event.setAmount(damage_dealt);
		}
	}

	void pushDamagePost(LivingDamageEvent event) {
		stage = Stage.DAMAGE_POST;
		damage = event;
		damage_dealt = event.getAmount();
		AttackEventHandler.getListeners().forEach(e -> e.onDamageFinalized(this, weapon));
	}

	void setupAttackerProfile(LivingEntity entity, ItemStack stack) {
		attacker = entity;
		weapon = stack;
	}

	public Stage getStage() {
		return stage;
	}

	@Nullable
	public AttackEntityEvent getPlayerAttackEntityEvent() {
		return player == null ? null : player.getPlayerAttackEntityEvent();
	}

	@Nullable
	public CriticalHitEvent getCriticalHitEvent() {
		return player == null ? null : player.getCriticalHitEvent();
	}

	@Nullable
	public LivingAttackEvent getLivingAttackEvent() {
		return attack;
	}

	@Nullable
	public LivingHurtEvent getLivingHurtEvent() {
		return hurt;
	}

	@Nullable
	public LivingDamageEvent getLivingDamageEvent() {
		return damage;
	}

	public LivingEntity getAttackTarget() {
		return target;
	}

	public LivingEntity getAttacker() {
		return attacker;
	}

	public ItemStack getWeapon() {
		return weapon;
	}

	public float getStrength() {
		return player == null ? 1 : player.getStrength();
	}

	public float getPreDamageOriginal() {
		if (stage.ordinal() < Stage.HURT_PRE.ordinal())
			throw new IllegalStateException("dealt damage not calculated yet");
		return damage_pre;
	}

	public float getPreDamage() {
		if (stage.ordinal() <= Stage.ACTUALLY_HURT_PRE.ordinal())
			throw new IllegalStateException("dealt damage not calculated yet");
		return damage_modified;
	}

	public void addHurtModifier(DamageModifier mod) {
		if (damageFrozen)
			throw new IllegalStateException("modify hurt damage only on onHurt event.");
		this.modifierHurt.add(mod);
	}

	public float getDamageDealt() {
		if (stage.ordinal() <= Stage.DAMAGE_PRE.ordinal())
			throw new IllegalStateException("actual damage not calculated yet");
		return damage_dealt;
	}

	public void addDealtModifier(DamageModifier mod) {
		if (stage != Stage.DAMAGE_PRE)
			throw new IllegalStateException("set actual damage only on onDamage event.");
		this.modifierDealt.add(mod);
	}

	public void setupPlayer(PlayerAttackCache prev) {
		player = prev;
		if (attacker == null) attacker = prev.getAttacker();
		if (weapon.isEmpty()) weapon = prev.getWeapon();
	}
}
