package dev.kast;

import dev.kast.config.ConfigManager;
import dev.kast.gui.ClickGuiScreen;
import dev.kast.module.Module;
import dev.kast.module.ModuleManager;
import dev.kast.render.Gfx;
import dev.kast.render.Proj;
import dev.kast.util.Compat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class KastClient implements ClientModInitializer {
	public static final String NAME = "KastDLC";
	private static boolean guiKeyWas;

	@Override
	public void onInitializeClient() {
		ModuleManager.init();
		ConfigManager.init();
		ConfigManager.load("default");

		ClientTickEvents.END_CLIENT_TICK.register(KastClient::tick);

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("kastdlc", "hud"), (graphics, delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.level == null || mc.gui.hud.isHidden()) return;
			float pt = delta.getGameTimeDeltaPartialTick(true);
			Gfx g = new Gfx(graphics);
			Proj.update(pt);
			for (Module m : ModuleManager.all()) {
				if (m.enabled && !m.locked) m.onHud(g, pt);
			}
		});
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) return;

		for (Module m : ModuleManager.all()) {
			if (m.enabled && !m.locked) m.onTick();
		}

		if (mc.gui.screen() != null) {
			guiKeyWas = true; // don't instantly reopen/close when a screen was just closed
			for (Module m : ModuleManager.all()) m.wasDown = true;
			return;
		}

		boolean gk = Compat.keyDown(ConfigManager.guiKey);
		if (gk && !guiKeyWas) mc.gui.setScreen(new ClickGuiScreen());
		guiKeyWas = gk;

		for (Module m : ModuleManager.all()) {
			boolean down = Compat.keyDown(m.key);
			if (down && !m.wasDown) m.toggle();
			m.wasDown = down;
		}
	}
}
