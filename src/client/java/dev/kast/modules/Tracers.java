package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Gfx;
import dev.kast.render.Proj;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;
import dev.kast.setting.ModeSetting;
import dev.kast.setting.NumSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class Tracers extends Module {
	private final BoolSetting players = add(new BoolSetting("Players", true));
	private final BoolSetting mobs = add(new BoolSetting("Mobs", false));
	private final ModeSetting origin = add(new ModeSetting("Origin", "Bottom", "Bottom", "Center"));
	private final NumSetting range = add(new NumSetting("Range", 64, 8, 160, 1));
	private final NumSetting width = add(new NumSetting("Width", 1.0, 0.5, 3.0, 0.5));

	public Tracers() {
		super("Tracers", "Lines to nearby entities", Category.VISUALS);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		if (mc.level == null || mc.player == null) return;
		float ox = g.width() / 2f;
		float oy = origin.is("Bottom") ? g.height() : g.height() / 2f;
		int drawn = 0;
		for (Entity e : mc.level.entitiesForRendering()) {
			if (drawn >= 24) break;
			if (e == mc.player || !(e instanceof LivingEntity)) continue;
			boolean p = e instanceof Player;
			if (p ? !players.get() : !(mobs.get() && e instanceof Enemy)) continue;
			if (mc.player.distanceTo(e) > range.get()) continue;
			double x = Mth.lerp(pt, e.xo, e.getX());
			double y = Mth.lerp(pt, e.yo, e.getY()) + e.getBbHeight() / 2.0;
			double z = Mth.lerp(pt, e.zo, e.getZ());
			float[] s = Proj.project(x, y, z);
			if (s == null) continue;
			int col = p ? Theme.accent(s[0]) : 0xFFFF5C5C;
			g.line(ox, oy, s[0], s[1], width.get() + 1f, 0x80000000);
			g.line(ox, oy, s[0], s[1], width.get(), col);
			drawn++;
		}
	}
}
