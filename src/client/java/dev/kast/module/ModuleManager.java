package dev.kast.module;

import dev.kast.modules.ArrayListHud;
import dev.kast.modules.Esp;
import dev.kast.modules.Fullbright;
import dev.kast.modules.Info;
import dev.kast.modules.Interface;
import dev.kast.modules.Keystrokes;
import dev.kast.modules.Radar;
import dev.kast.modules.Tracers;
import dev.kast.modules.Watermark;
import dev.kast.modules.Waves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	public static Interface iface;

	private ModuleManager() {}

	public static void init() {
		iface = new Interface();
		MODULES.add(new Watermark());
		MODULES.add(new ArrayListHud());
		MODULES.add(new Info());
		MODULES.add(new Keystrokes());
		MODULES.add(new Radar());
		MODULES.add(new Waves());
		MODULES.add(new Esp());
		MODULES.add(new Tracers());
		MODULES.add(new Fullbright());
		MODULES.add(iface);

		// defaults
		for (Module m : MODULES) {
			if (m instanceof Watermark || m instanceof ArrayListHud || m instanceof Info) m.set(true);
		}
	}

	public static List<Module> all() {
		return Collections.unmodifiableList(MODULES);
	}

	public static List<Module> in(Category c) {
		List<Module> out = new ArrayList<>();
		for (Module m : MODULES) if (m.category == c) out.add(m);
		return out;
	}
}
