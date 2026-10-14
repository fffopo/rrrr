package dev.kast.render;

/** Easing functions, input and output in 0..1. */
public final class Ease {
	private Ease() {}

	public static float outCubic(float t) {
		float f = 1f - t;
		return 1f - f * f * f;
	}

	public static float inOutCubic(float t) {
		return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
	}
}
