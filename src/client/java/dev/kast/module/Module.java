package dev.kast.module;

import dev.kast.render.Anim;
import dev.kast.render.Gfx;
import dev.kast.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
	public final String name;
	public final String description;
	public final Category category;
	public boolean enabled;
	public int key = -1;
	public boolean showInList = true;
	/** Locked modules (the Interface module) are always on and cannot be toggled. */
	public boolean locked;
	public boolean wasDown;
	public final List<Setting> settings = new ArrayList<>();

	// UI animation state
	public final Anim stateAnim = new Anim(0);
	public final Anim hoverAnim = new Anim(0);
	public final Anim listAnim = new Anim(0);

	protected final Minecraft mc = Minecraft.getInstance();

	protected Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <T extends Setting> T add(T s) {
		settings.add(s);
		return s;
	}

	public void toggle() {
		set(!enabled);
	}

	public void set(boolean v) {
		if (locked || v == enabled) return;
		enabled = v;
		if (v) onEnable();
		else onDisable();
	}

	/** Text shown after the name in the module list, or null. */
	public String suffix() {
		return null;
	}

	public void onEnable() {}

	public void onDisable() {}

	public void onTick() {}

	public void onHud(Gfx g, float partialTick) {}
}
