package dev.kast.setting;

import com.google.gson.JsonElement;

import java.util.function.BooleanSupplier;

public abstract class Setting {
	public final String name;
	public BooleanSupplier visible = () -> true;

	protected Setting(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public <T extends Setting> T visibleIf(BooleanSupplier v) {
		this.visible = v;
		return (T) this;
	}

	public abstract JsonElement save();

	public abstract void load(JsonElement e);
}
