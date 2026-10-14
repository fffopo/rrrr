package dev.kast.render;

/**
 * Frame clock for the GUI. beginFrame() is called once per rendered frame,
 * everything that animates by progress (Tween) is advanced with dt(), so
 * animation speed never depends on FPS.
 */
public final class AnimationController {
	private static long last = System.nanoTime();
	private static float dt = 1f / 60f;

	private AnimationController() {}

	public static void beginFrame() {
		long n = System.nanoTime();
		dt = Math.max(0f, Math.min((n - last) / 1_000_000_000f, 0.05f));
		last = n;
	}

	public static float dt() {
		return dt;
	}
}
