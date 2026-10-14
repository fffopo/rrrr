package dev.kast.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumSetting extends Setting {
	public double value;
	public final double min, max, step;

	public NumSetting(String name, double def, double min, double max, double step) {
		super(name);
		this.value = def;
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public float get() {
		return (float) value;
	}

	public int getInt() {
		return (int) Math.round(value);
	}

	public void set(double v) {
		if (step > 0) v = Math.round(v / step) * step;
		value = Math.max(min, Math.min(max, v));
	}

	public String display() {
		return step >= 1 ? String.valueOf((int) Math.round(value)) : String.format(java.util.Locale.ROOT, "%.2f", value);
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement e) {
		set(e.getAsDouble());
	}
}
