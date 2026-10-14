package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.Proj;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;
import dev.kast.setting.NumSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class Esp extends Module {
	private final BoolSetting players = add(new BoolSetting("Players", true));
	private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
	private final BoolSetting animals = add(new BoolSetting("Animals", false));
	private final BoolSetting items = add(new BoolSetting("Items", false));
	private final BoolSetting names = add(new BoolSetting("Names", true));
	private final BoolSetting health = add(new BoolSetting("Health bar", true));
	private final NumSetting range = add(new NumSetting("Range", 64, 8, 160, 1));
	private final NumSetting thickness = add(new NumSetting("Thickness", 1.0, 0.5, 3.0, 0.5));

	public Esp() {
		super("ESP", "2D boxes around entities", Category.VISUALS);
	}

	private int colorFor(Entity e) {
		if (e instanceof Player) return Theme.accent(0);
		if (e instanceof Enemy) return 0xFFFF5C5C;
		if (e instanceof ItemEntity) return 0xFFFFD84D;
		return 0xFF62E08C;
	}

	private boolean accept(Entity e) {
		if (e == mc.player) return false;
		if (e instanceof Player) return players.get();
		if (e instanceof Enemy) return mobs.get();
		if (e instanceof ItemEntity) return items.get();
		return e instanceof LivingEntity && animals.get();
	}

	@Override
	public void onHud(Gfx g, float pt) {
		if (mc.level == null || mc.player == null) return;
		float th = thickness.get();
		double maxD = range.get();
		for (Entity e : mc.level.entitiesForRendering()) {
			if (!accept(e) || mc.player.distanceTo(e) > maxD) continue;
			double x = Mth.lerp(pt, e.xo, e.getX());
			double y = Mth.lerp(pt, e.yo, e.getY());
			double z = Mth.lerp(pt, e.zo, e.getZ());
			double hw = e.getBbWidth() / 2.0, hh = e.getBbHeight();
			float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
			boolean ok = true;
			for (int i = 0; i < 8 && ok; i++) {
				float[] p = Proj.project(x + ((i & 1) == 0 ? -hw : hw), y + ((i & 2) == 0 ? 0 : hh), z + ((i & 4) == 0 ? -hw : hw));
				if (p == null) {
					ok = false;
					break;
				}
				minX = Math.min(minX, p[0]);
				maxX = Math.max(maxX, p[0]);
				minY = Math.min(minY, p[1]);
				maxY = Math.max(maxY, p[1]);
			}
			if (!ok || maxX - minX < 2 || maxY - minY < 2) continue;

			int col = colorFor(e);
			float w = maxX - minX, h = maxY - minY;
			g.outline(minX - 0.5f, minY - 0.5f, w + 1, h + 1, th + 1f, 0xB0000000);
			g.outline(minX, minY, w, h, th, col);

			if (health.get() && e instanceof LivingEntity le) {
				float f = Mth.clamp(le.getHealth() / Math.max(1f, le.getMaxHealth()), 0f, 1f);
				float bx = minX - 4.5f;
				g.round(bx, minY, 2.5f, h, 1.2f, 0xB0000000);
				g.round(bx, minY + h * (1f - f), 2.5f, h * f, 1.2f, Col.lerp(0xFFFF4D4D, 0xFF4DFF7A, f));
			}
			if (names.get()) {
				String n = e instanceof ItemEntity ie ? ie.getItem().getHoverName().getString() : e.getName().getString();
				float tw = g.width(n) * 0.85f + 6;
				float tx = minX + w / 2f - tw / 2f;
				g.round(tx, minY - 12.5f, tw, 11, 4, 0xB0101015);
				g.text(n, tx + 3, minY - 10.5f, 0xFFFFFFFF, 0.85f);
			}
		}
	}
}
