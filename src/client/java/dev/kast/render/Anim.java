package dev.kast.render;

/** Smooth exponential animation value. Frame-rate independent. */
public final class Anim {
	private float value;
	private float target;
	private long last = System.nanoTime();

	public Anim(float start) {
		this.value = start;
		this.target = start;
	}

	public Anim set(float t) {
		this.target = t;
		return this;
	}

	public void snap(float v) {
		this.value = v;
		this.target = v;
	}

	public float target() {
		return target;
	}

	public float get() {
		return get(14f);
	}

	public float get(float speed) {
		long n = System.nanoTime();
		float dt = Math.min((n - last) / 1_000_000_000f, 0.1f);
		last = n;
		value += (target - value) * (1f - (float) Math.exp(-speed * dt));
		if (Math.abs(target - value) < 0.0005f) value = target;
		return value;
	}
}
