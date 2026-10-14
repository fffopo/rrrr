package dev.kast.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kast.module.Module;
import dev.kast.module.ModuleManager;
import dev.kast.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/** JSON configs in .minecraft/config/kastdlc/. */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Path dir;
	/** GLFW key that opens the Click GUI. */
	public static int guiKey = 344; // RIGHT SHIFT

	private ConfigManager() {}

	public static void init() {
		dir = FabricLoader.getInstance().getConfigDir().resolve("kastdlc");
		try {
			Files.createDirectories(dir);
		} catch (IOException ignored) {
		}
	}

	public static String clean(String name) {
		return name == null ? "" : name.replaceAll("[^a-zA-Z0-9_\\-]", "");
	}

	public static List<String> list() {
		List<String> out = new ArrayList<>();
		try (Stream<Path> s = Files.list(dir)) {
			s.map(p -> p.getFileName().toString())
				.filter(n -> n.endsWith(".json"))
				.forEach(n -> out.add(n.substring(0, n.length() - 5)));
		} catch (IOException ignored) {
		}
		Collections.sort(out);
		return out;
	}

	public static boolean save(String name) {
		name = clean(name);
		if (name.isEmpty()) return false;
		JsonObject root = new JsonObject();
		root.addProperty("guiKey", guiKey);
		JsonObject mods = new JsonObject();
		for (Module m : ModuleManager.all()) {
			JsonObject o = new JsonObject();
			o.addProperty("enabled", m.enabled);
			o.addProperty("key", m.key);
			o.addProperty("showInList", m.showInList);
			JsonObject st = new JsonObject();
			for (Setting s : m.settings) st.add(s.name, s.save());
			o.add("settings", st);
			mods.add(m.name, o);
		}
		root.add("modules", mods);
		try {
			Files.writeString(dir.resolve(name + ".json"), GSON.toJson(root));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean load(String name) {
		name = clean(name);
		Path p = dir.resolve(name + ".json");
		if (name.isEmpty() || !Files.exists(p)) return false;
		try {
			JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
			if (root.has("guiKey")) guiKey = root.get("guiKey").getAsInt();
			JsonObject mods = root.getAsJsonObject("modules");
			if (mods == null) return true;
			for (Module m : ModuleManager.all()) {
				if (!mods.has(m.name)) continue;
				try {
					JsonObject o = mods.getAsJsonObject(m.name);
					if (o.has("key")) m.key = o.get("key").getAsInt();
					if (o.has("showInList")) m.showInList = o.get("showInList").getAsBoolean();
					if (o.has("settings")) {
						JsonObject st = o.getAsJsonObject("settings");
						for (Setting s : m.settings) {
							JsonElement e = st.get(s.name);
							if (e != null) {
								try {
									s.load(e);
								} catch (RuntimeException ignored) {
								}
							}
						}
					}
					if (o.has("enabled")) m.set(o.get("enabled").getAsBoolean());
				} catch (RuntimeException ignored) {
				}
			}
			return true;
		} catch (IOException | RuntimeException e) {
			return false;
		}
	}

	public static boolean delete(String name) {
		try {
			return Files.deleteIfExists(dir.resolve(clean(name) + ".json"));
		} catch (IOException e) {
			return false;
		}
	}
}
