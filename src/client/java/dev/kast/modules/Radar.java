package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;
import dev.kast.setting.NumSetting;
import dev.kast.util.Compat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class Radar extends Module {
	private final NumSetting size = add(new NumSetting("Size", 90, 60, 160, 2));
	private final NumSetting range = add(new NumSetting("Range", 40, 10, 128, 1));
	private final BoolSetting mobs = add(new BoolSetting("Mobs", false));

	public Radar() {
		super("Radar", "Round radar of nearby entities", Category.VISUALS);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		if (mc.player == null || mc.level == null) return;
		float d = size.get();
		float cx = g.width() - d / 2f - 8;
		float cy = g.height() - d / 2f - 8;
		g.shadow(cx - d / 2f, cy - d / 2f, d, d, d / 2f, 6, 0x80000000);
		g.circle(cx, cy, d / 2f, 0xC0101015);
		g.circle(cx, cy, d / 2f - 1f, 0xC018181F);
		g.circle(cx, cy, d / 4f, 0x30FFFFFF);
		g.circle(cx, cy, d / 4f - 0.6f, 0xC018181F);
		g.rect(cx - 0.5f, cy - d / 2f + 3, 1, d - 6, 0x25FFFFFF);
		g.rect(cx - d / 2f + 3, cy - 0.5f, d - 6, 1, 0x25FFFFFF);

		double yaw = Math.toRadians(Compat.yaw(mc.player, pt));
		double fx = -Math.sin(yaw), fz = Math.cos(yaw);
		double rx = -Math.cos(yaw), rz = -Math.sin(yaw);
		double r = range.get();
		for (Entity e : mc.level.entitiesForRendering()) {
			if (e == mc.player || !(e instanceof LivingEntity)) continue;
			boolean player = e instanceof Player;
			if (!player && !mobs.get()) continue;
			double dx = Mth.lerp(pt, e.xo, e.getX()) - Mth.lerp(pt, mc.player.xo, mc.player.getX());
			double dz = Mth.lerp(pt, e.zo, e.getZ()) - Mth.lerp(pt, mc.player.zo, mc.player.getZ());
			double sx = (dx * rx + dz * rz) / r;
			double sy = -(dx * fx + dz * fz) / r;
			double len = Math.hypot(sx, sy);
			if (len > 1) {
				sx /= len;
				sy /= len;
			}
			float px = cx + (float) sx * (d / 2f - 5);
			float py = cy + (float) sy * (d / 2f - 5);
			g.circle(px, py, 2.2f, player ? 0xFFFFFFFF : 0xFFFF6B6B);
		}
		g.circle(cx, cy, 2.6f, Theme.accent(0));
	}
}
