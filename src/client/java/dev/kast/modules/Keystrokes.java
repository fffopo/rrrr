package dev.kast.modules;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.Anim;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.Theme;
import dev.kast.setting.NumSetting;
import net.minecraft.client.KeyMapping;

public final class Keystrokes extends Module {
	private final NumSetting x = add(new NumSetting("X offset", 8, 0, 400, 1));
	private final NumSetting size = add(new NumSetting("Key size", 20, 14, 32, 1));
	private final Anim[] anims = {new Anim(0), new Anim(0), new Anim(0), new Anim(0), new Anim(0)};

	public Keystrokes() {
		super("Keystrokes", "WASD and space overlay", Category.PLAYER);
	}

	@Override
	public void onHud(Gfx g, float pt) {
		float k = size.get();
		float gap = 3;
		float bx = x.get() + 6;
		float by = g.height() / 2f - (k * 3 + gap * 2) / 2f;
		key(g, 0, mc.options.keyUp, "W", bx + k + gap, by, k, k);
		key(g, 1, mc.options.keyLeft, "A", bx, by + k + gap, k, k);
		key(g, 2, mc.options.keyDown, "S", bx + k + gap, by + k + gap, k, k);
		key(g, 3, mc.options.keyRight, "D", bx + (k + gap) * 2, by + k + gap, k, k);
		key(g, 4, mc.options.keyJump, "SPACE", bx, by + (k + gap) * 2, k * 3 + gap * 2, k * 0.7f);
	}

	private void key(Gfx g, int i, KeyMapping km, String label, float x, float y, float w, float h) {
		float a = anims[i].set(km.isDown() ? 1f : 0f).get(18f);
		int bg = Col.lerp(0xB0101015, Col.alpha(Theme.accent(y * 4), 0xC0), a);
		g.round(x, y, w, h, 5, bg);
		g.textCentered(label, x + w / 2f, y + (h - 8) / 2f, Col.lerp(Theme.TEXT_DIM, 0xFFFFFFFF, a), 1f);
	}
}
