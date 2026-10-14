package dev.kast.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.kast.render.Col;

/** Hue-slider colour. */
public final class ColorSetting extends Setting {
	public float hue;
	public float sat = 0.62f;
	public float bri = 1f;

	public ColorSetting(String name, float hue) {
		super(name);
		this.hue = hue;
	}

	public int get() {
		return Col.hsb(hue, sat, bri);
	}

	public int get(float hueShift) {
		return Col.hsb(hue + hueShift, sat, bri);
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(hue);
	}

	@Override
	public void load(JsonElement e) {
		hue = Math.max(0f, Math.min(1f, e.getAsFloat()));
	}
}
