package dev.xkmc.l2damagetracker.contents.materials.vanilla;

import dev.xkmc.l2damagetracker.contents.materials.api.ITool;
import dev.xkmc.l2damagetracker.contents.materials.generic.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public enum Tools implements ITool {
	SWORD(ItemTags.SWORDS, GenericSwordItem::new, 1f, 0, 1.6f),
	AXE(ItemTags.AXES, GenericAxeItem::new, 1.4f, 0, 1),
	SHOVEL(ItemTags.SHOVELS, GenericShovelItem::new, 0.7f, 0, 1f),
	PICKAXE(ItemTags.PICKAXES, GenericPickaxeItem::new, 0.7f, 0, 1.2f),
	HOE(ItemTags.HOES, GenericHoeItem::new, 0.5f, -5, 4f);

	final TagKey<Item> tag;

	private final RawToolFactory fac;
	private final float damage_scale, damage_offset, speed_scale;

	Tools(TagKey<Item> tag, RawToolFactory fac, float damage_scale, float damage_offset, float speed_scale) {
		this.tag = tag;
		this.fac = fac;
		this.damage_scale = damage_scale;
		this.damage_offset = damage_offset;
		this.speed_scale = speed_scale;
	}

	public int getDamage(int base_damage) {
		return Math.round((base_damage + damage_offset) * damage_scale);
	}

	public float getSpeed(float base_speed) {
		return Mth.clamp(Math.round(base_speed * speed_scale * 10) * 0.1f, 0.1f, 4f);
	}

	@Override
	public Item create(Tier tier, int damage, float speed, Item.Properties prop, ExtraToolConfig config) {
		return fac.get(tier, damage, speed, prop, config);
	}
}
