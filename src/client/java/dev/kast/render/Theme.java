package dev.kast.render;

import dev.kast.module.ModuleManager;
import dev.kast.modules.Interface;

/** Accent colour used by the GUI and the HUD. Controlled by the Interface module. */
public final class Theme {
	private Theme() {}

	public static int accent() {
		return accent(0);
	}

	/** @param offset position along the gradient / rainbow, in pixels. */
	public static int accent(float offset) {
		Interface i = ModuleManager.iface;
		if (i == null) return 0xFF8A7CFF;
		float t = (System.currentTimeMillis() % 6000L) / 6000f;
		if (i.style.is("Rainbow")) {
			return Col.hsb(t + offset * 0.0015f, 0.6f, 1f);
		}
		if (i.style.is("Gradient")) {
			float w = (float) (Math.sin((t * 2 - offset * 0.003f) * Math.PI) * 0.5 + 0.5);
			return Col.lerp(i.color.get(), i.color.get(0.14f), w);
		}
		return i.color.get();
	}

	public static final int BG = 0xF0101015;
	public static final int PANEL = 0xFF17171E;
	public static final int CARD = 0xFF1C1C25;
	public static final int CARD_HOVER = 0xFF242430;
	public static final int TEXT = 0xFFF2F2F8;
	public static final int TEXT_DIM = 0xFF8C8C9C;
	public static final int OFF = 0xFF2E2E3A;
}
