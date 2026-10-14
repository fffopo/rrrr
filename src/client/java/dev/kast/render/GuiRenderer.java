package dev.kast.render;

/** Reusable GUI widgets drawn on top of Gfx (rounded shapes, switches, icons). */
public final class GuiRenderer {
	private GuiRenderer() {}

	/** Dark glass panel: soft shadow, translucent body and a faint inner rim. */
	public static void panel(Gfx g, float x, float y, float w, float h, float r) {
		g.shadow(x, y, w, h, r, 12, 0xA0000000);
		g.round(x, y, w, h, r, 0xD2080B16);
		g.round(x + 1, y + 1, w - 2, h - 2, r - 1, 0x10FFFFFF);
	}

	public static void toggle(Gfx g, float x, float y, float st, int accent) {
		g.round(x, y, 20, 10, 5, Col.lerp(0xFF2A2E40, accent, st));
		g.circle(x + 5 + 10 * st, y + 5, 3.4f, 0xFFFFFFFF);
	}

	/** Three dots that rotate into a vertical column when open = 1. */
	public static void dots(Gfx g, float cx, float cy, float open, int color) {
		double a = Math.toRadians(90.0 * open);
		float c = (float) Math.cos(a), s = (float) Math.sin(a);
		for (int i = -1; i <= 1; i++) {
			float o = i * 3.4f;
			g.circle(cx + o * c, cy + o * s, 0.95f, color);
		}
	}

	/** Chevron that flips from "v" (closed) to "^" (open). */
	public static void chevron(Gfx g, float cx, float cy, float open, int color) {
		float f = 1f - 2f * open;
		g.line(cx - 3, cy - 1.4f * f, cx, cy + 1.4f * f, 1f, color);
		g.line(cx, cy + 1.4f * f, cx + 3, cy - 1.4f * f, 1f, color);
	}

	public static void cross(Gfx g, float cx, float cy, float r, int color) {
		g.line(cx - r, cy - r, cx + r, cy + r, 1f, color);
		g.line(cx - r, cy + r, cx + r, cy - r, 1f, color);
	}

	public static void scrollbar(Gfx g, float x, float y, float viewH, float scroll, float contentH) {
		if (contentH <= viewH + 0.5f) return;
		float bh = Math.max(14f, viewH * viewH / contentH);
		float by = y + (viewH - bh) * (scroll / (contentH - viewH));
		g.round(x, by, 2, bh, 1, 0x58FFFFFF);
	}

	public static void chip(Gfx g, float x, float y, float w, float h, int color) {
		g.round(x, y, w, h, 5, color);
	}
}
