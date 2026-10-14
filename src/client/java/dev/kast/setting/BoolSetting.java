package dev.kast.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BoolSetting extends Setting {
	public boolean value;

	public BoolSetting(String name, boolean def) {
		super(name);
		this.value = def;
	}

	public boolean get() {
		return value;
	}

	@Override
	public JsonElement save() {
		return new JsonPrimitive(value);
	}

	@Override
	public void load(JsonElement e) {
		value = e.getAsBoolean();
	}
}
