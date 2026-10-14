package dev.kast.render;

/** ARGB colour helpers. */
public final class Col {
	private Col() {}

	public static int alpha(int c, int a) {
		return (Math.max(0, Math.min(255, a)) << 24) | (c & 0xFFFFFF);
	}

	public static int mulAlpha(int c, float f) {
		return alpha(c, Math.round((c >>> 24) * f));
	}

	public static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int aa = (int) (((a >>> 24) & 255) + (((b >>> 24) & 255) - ((a >>> 24) & 255)) * t);
		int ar = (int) (((a >> 16) & 255) + (((b >> 16) & 255) - ((a >> 16) & 255)) * t);
		int ag = (int) (((a >> 8) & 255) + (((b >> 8) & 255) - ((a >> 8) & 255)) * t);
		int ab = (int) ((a & 255) + ((b & 255) - (a & 255)) * t);
		return (aa << 24) | (ar << 16) | (ag << 8) | ab;
	}

	public static int hsb(float h, float s, float v) {
		h = h - (float) Math.floor(h);
		float c = v * s;
		float x = c * (1 - Math.abs((h * 6) % 2 - 1));
		float m = v - c;
		float r, g, b;
		switch ((int) (h * 6)) {
			case 0 -> { r = c; g = x; b = 0; }
			case 1 -> { r = x; g = c; b = 0; }
			case 2 -> { r = 0; g = c; b = x; }
			case 3 -> { r = 0; g = x; b = c; }
			case 4 -> { r = x; g = 0; b = c; }
			default -> { r = c; g = 0; b = x; }
		}
		return 0xFF000000 | ((int) ((r + m) * 255) << 16) | ((int) ((g + m) * 255) << 8) | (int) ((b + m) * 255);
	}
}
