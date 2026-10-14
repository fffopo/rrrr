package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.BoolSetting;

public final class Watermark extends Module {
	private final BoolSetting showFps = add(new BoolSetting("FPS", true));
	private final BoolSetting showName = add(new BoolSetting("Username", true));

	public Watermark() {
		super("Watermark", "Client tag in the top-left corner", Category.VISUALS);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		String title = "KastDLC";
		StringBuilder sb = new StringBuilder();
		if (showName.get()) sb.append(mc.getUser().getName());
		if (showFps.get()) {
			if (sb.length() > 0) sb.append("  |  ");
			sb.append(mc.getFps()).append(" fps");
		}
		String info = sb.toString();
		float tw = g.width(title) + 4;
		float iw = info.isEmpty() ? 0 : g.width(info) + 10;
		float w = 10 + tw + iw + 6;
		float x = 6, y = 6, h = 17;
		g.shadow(x, y, w, h, 6, 6, 0x90000000);
		g.round(x, y, w, h, 6, 0xE0121218);
		g.round(x + 3, y + 3, tw + 8, h - 6, 4, Theme.accent(0) & 0x00FFFFFF | 0x40000000);
		g.text(title, x + 7, y + 4.5f, Theme.accent(0));
		if (!info.isEmpty()) g.text(info, x + 10 + tw + 6, y + 4.5f, Theme.TEXT);
	}
}
