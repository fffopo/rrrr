package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.setting.ColorSetting;
import dev.kast.setting.ModeSetting;

public final class Interface extends Module {
	public final ColorSetting color = add(new ColorSetting("Accent", 0.74f));
	public final ModeSetting style = add(new ModeSetting("Style", "Gradient", "Static", "Gradient", "Rainbow"));

	public Interface() {
		super("Interface", "Colours and style of the client", Category.MISC);
		this.locked = true;
		this.enabled = true;
		this.showInList = false;
	}
}
