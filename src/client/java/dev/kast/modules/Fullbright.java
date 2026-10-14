package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Client-side night vision effect: bright everywhere, no mixin required. */
public final class Fullbright extends Module {
	private boolean added;

	public Fullbright() {
		super("Fullbright", "Everything is fully lit", Category.VISUALS);
	}

	@Override
	public void onTick() {
		if (mc.player == null) return;
		MobEffectInstance cur = mc.player.getEffect(MobEffects.NIGHT_VISION);
		if (cur == null || cur.getDuration() < 400) {
			mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2000, 0, false, false, false));
			added = true;
		}
	}

	@Override
	public void onDisable() {
		if (added && mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
		added = false;
	}
}
