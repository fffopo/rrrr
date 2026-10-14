package dev.kast.render;

/**
 * Progress animation 0..1 with a fixed duration in seconds (time based, not frame based).
 * Call update(dt) once per frame, then read out() / inOut() any number of times.
 */
public final class Tween {
	private float t;
	private boolean on;
	private final float duration;

	public Tween(float durationSec, boolean startOn) {
		this.duration = durationSec;
		this.on = startOn;
		this.t = startOn ? 1f : 0f;
	}

	public void set(boolean on) {
		this.on = on;
	}

	public boolean target() {
		return on;
	}

	public void snap(boolean on) {
		this.on = on;
		this.t = on ? 1f : 0f;
	}

	public void update(float dt) {
		t = Math.max(0f, Math.min(1f, t + (on ? dt : -dt) / duration));
	}

	public float linear() {
		return t;
	}

	public float out() {
		return Ease.outCubic(t);
	}

	public float inOut() {
		return Ease.inOutCubic(t);
	}
}
