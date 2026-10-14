package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.module.ModuleManager;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;

import java.util.ArrayList;
import java.util.List;

public final class ArrayListHud extends Module {
	private final BoolSetting bar = add(new BoolSetting("Side bar", true));
	private final BoolSetting background = add(new BoolSetting("Background", true));

	public ArrayListHud() {
		super("ArrayList", "List of enabled modules", Category.VISUALS);
	}

	private static String label(Module m) {
		return m.suffix() == null ? m.name : m.name + " " + m.suffix();
	}

	@Override
	public void onHud(Gfx g, float pt) {
		List<Module> list = new ArrayList<>();
		for (Module m : ModuleManager.all()) {
			m.listAnim.set(m.enabled && m.showInList && !m.locked && m != this ? 1f : 0f);
			if (m.listAnim.get(12f) > 0.01f) list.add(m);
		}
		list.sort((a, b) -> Float.compare(g.width(label(b)), g.width(label(a))));

		float y = 6;
		int sw = g.width();
		int i = 0;
		for (Module m : list) {
			float a = m.listAnim.get(12f);
			String t = label(m);
			float w = g.width(t) + 10;
			float h = 13;
			float x = sw - w - 6 + (1f - a) * (w + 12);
			int col = Theme.accent(y * 4);
			g.alpha = a;
			if (background.get()) g.round(x, y, w, h, 4, 0xB0101015);
			if (bar.get()) g.round(x + w - 2, y + 2, 2, h - 4, 1, col);
			g.text(t, x + 4, y + 2.5f, col);
			g.alpha = 1f;
			y += (h + 2) * a;
			i++;
		}
	}
}
