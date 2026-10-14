package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;

import java.util.Locale;

public final class Info extends Module {
	private final BoolSetting coords = add(new BoolSetting("Coordinates", true));
	private final BoolSetting speed = add(new BoolSetting("Speed", true));

	public Info() {
		super("Info", "Coordinates and speed", Category.PLAYER);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		if (mc.player == null) return;
		String line1 = null, line2 = null;
		if (coords.get()) {
			line1 = String.format(Locale.ROOT, "XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
		}
		if (speed.get()) {
			double dx = mc.player.getX() - mc.player.xo;
			double dz = mc.player.getZ() - mc.player.zo;
			line2 = String.format(Locale.ROOT, "Speed  %.2f b/s", Math.hypot(dx, dz) * 20.0);
		}
		float h = g.height();
		float y = h - 6;
		if (line2 != null) y = row(g, line2, y);
		if (line1 != null) row(g, line1, y);
	}

	private float row(Gfx g, String t, float bottom) {
		float w = g.width(t) + 12;
		float x = 6, hh = 15, y = bottom - hh;
		g.round(x, y, w, hh, 5, 0xB0101015);
		g.round(x + 3, y + 4, 2, hh - 8, 1, Theme.accent(y * 3));
		g.text(t, x + 8, y + 3.5f, Theme.TEXT);
		return y - 3;
	}
}
