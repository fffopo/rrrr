package dev.kast.gui;

final class Ui {
	private Ui() {}

	static boolean hit(double mx, double my, float x, float y, float w, float h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	static float clamp(float v, float lo, float hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	/** Vertical offset that centres an 8px glyph line in a box of height h. */
	static float ty(float y, float h) {
		return y + (h - 8f) / 2f;
	}
}
