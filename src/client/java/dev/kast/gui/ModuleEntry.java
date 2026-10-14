package dev.kast.gui;

import dev.kast.module.Module;
import dev.kast.render.Anim;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.GuiRenderer;
import dev.kast.render.Theme;
import dev.kast.render.Tween;
import dev.kast.util.Keys;

import java.util.Locale;

/** One module row inside a category panel, with its expandable settings area. */
public final class ModuleEntry {
	final Module m;
	final ModuleSettingsPanel sp;
	private final String lower;

	private final Tween openT = new Tween(0.30f, false);   // settings expand / collapse
	private final Tween matchT = new Tween(0.22f, true);   // search filter show / hide
	private final Anim hover = new Anim(0);
	private final Anim state;
	private final Anim flash = new Anim(0);

	// hit geometry of the last rendered frame
	private float rx, ry, rw;
	private boolean visible;

	public ModuleEntry(Module m, ClickGuiScreen owner) {
		this.m = m;
		this.lower = m.name.toLowerCase(Locale.ROOT);
		this.sp = new ModuleSettingsPanel(m, owner);
		this.state = new Anim(m.enabled ? 1f : 0f);
	}

	public boolean matches(String q) {
		return lower.contains(q);
	}

	public void flash() {
		flash.snap(1f);
		flash.set(0f);
	}

	public void update(float dt, boolean match) {
		matchT.set(match);
		matchT.update(dt);
		openT.update(dt);
		sp.update(dt);
	}

	private float settingsHeight() {
		float oe = openT.inOut();
		return oe > 0.001f ? (sp.contentHeight() + 4f) * oe : 0f;
	}

	/** Layout height in the panel list, includes the filter animation. */
	public float height() {
		float me = matchT.inOut();
		if (me <= 0.001f) return 0f;
		float sh = settingsHeight();
		return (CategoryPanel.ROW_H + (sh > 0 ? sh + 2f : 0f) + CategoryPanel.GAP) * me;
	}

	public void markHidden() {
		visible = false;
	}

	public void render(Gfx g, float x, float y, float w, int mx, int my, boolean hoverOk, int accent) {
		float me = matchT.out();
		visible = me > 0.02f;
		if (!visible) return;
		rx = x;
		ry = y;
		rw = w;
		float a0 = g.alpha;
		g.alpha = a0 * me;

		boolean hov = hoverOk && Ui.hit(mx, my, x, y, w, CategoryPanel.ROW_H);
		float h = hover.set(hov ? 1f : 0f).get(18f);
		float st = state.set(m.enabled ? 1f : 0f).get(14f);
		float fl = flash.get(5f);

		int on = Col.lerp(0xC02B2E70, Col.alpha(accent, 0xC0), 0.28f);
		int bg = Col.lerp(0xB0070A12, on, st);
		g.round(x, y, w, CategoryPanel.ROW_H, 5, bg);
		if (h > 0.01f) g.round(x, y, w, CategoryPanel.ROW_H, 5, Col.alpha(0xFFFFFFFF, Math.round(0x14 * h)));
		if (fl > 0.01f) g.round(x, y, w, CategoryPanel.ROW_H, 5, Col.alpha(accent, Math.round(0x90 * fl)));

		int txt = Col.lerp(0xFFB4B9CC, 0xFFFFFFFF, Math.max(st, h * 0.7f));
		g.text(m.name, x + 8, Ui.ty(y, CategoryPanel.ROW_H), txt);

		float oe = openT.inOut();
		if (m.key > 0) {
			String kn = Keys.name(m.key);
			g.text(kn, x + w - 24 - g.width(kn) * 0.75f, Ui.ty(y, CategoryPanel.ROW_H), Theme.TEXT_DIM, 0.75f);
		}
		GuiRenderer.dots(g, x + w - 12, y + CategoryPanel.ROW_H / 2f, oe, Col.lerp(0xFF7C8196, 0xFFFFFFFF, Math.max(oe, h)));

		float sh = settingsHeight();
		if (sh > 0.5f) {
			float ay = y + CategoryPanel.ROW_H + 2f;
			g.alpha = a0 * me * Math.min(1f, oe * 1.3f);
			g.round(x, ay, w, sh, 5, 0x80050810);
			g.scissor(x, ay, w, sh);
			sp.render(g, x, ay - (1f - oe) * 8f, w, sh, mx, my);
			g.unscissor();
		}
		g.alpha = a0;
	}

	/** @return true if the click was consumed by this entry. */
	public boolean click(double mx, double my, int b, ClickGuiScreen owner) {
		if (!visible) return false;
		if (Ui.hit(mx, my, rx, ry, rw, CategoryPanel.ROW_H)) {
			if (b == 0) {
				if (!m.locked) m.toggle();
			} else if (b == 1) {
				if (sp.isEmpty()) owner.notify(m.name + ": no settings");
				else openT.set(!openT.target());
			} else if (b == 2 && !m.locked) {
				owner.beginBind(m);
			}
			return true;
		}
		if (openT.linear() > 0.3f && sp.contains(mx, my)) {
			sp.click(mx, my, b);
			if (sp.isDragging()) owner.setActiveSettings(sp);
			return true;
		}
		return false;
	}
}
