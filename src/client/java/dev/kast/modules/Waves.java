package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.NumSetting;

public final class Waves extends Module {
	private final NumSetting height = add(new NumSetting("Height", 40, 10, 140, 1));
	private final NumSetting speed = add(new NumSetting("Speed", 1.0, 0.2, 4.0, 0.1));
	private final NumSetting opacity = add(new NumSetting("Opacity", 40, 8, 120, 1));

	public Waves() {
		super("Waves", "Animated waves at the bottom of the screen", Category.VISUALS);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		int w = g.width(), h = g.height();
		float t = (System.currentTimeMillis() % 1_000_000L) / 1000f * speed.get();
		float amp = height.get();
		int a = opacity.getInt();
		for (int layer = 0; layer < 3; layer++) {
			int col = Col.alpha(Theme.accent(layer * 300), a);
			float phase = layer * 1.7f;
			float freq = 0.012f + layer * 0.004f;
			float la = amp * (1f - layer * 0.28f);
			for (int x = 0; x < w; x += 2) {
				double wave = Math.sin(x * freq + t * (1.2 + layer * 0.5) + phase) * 0.5
					+ Math.sin(x * freq * 2.3 - t * 0.9 + phase) * 0.25;
				float top = h - la * 0.5f - (float) wave * la * 0.5f;
				g.rect(x, top, 2, h - top, col);
			}
		}
	}
}
