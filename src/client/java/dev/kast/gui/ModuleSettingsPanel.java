package dev.kast.gui;

import dev.kast.module.Module;
import dev.kast.render.Anim;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.GuiRenderer;
import dev.kast.render.Theme;
import dev.kast.render.Tween;
import dev.kast.setting.BoolSetting;
import dev.kast.setting.ColorSetting;
import dev.kast.setting.ModeSetting;
import dev.kast.setting.NumSetting;
import dev.kast.setting.Setting;
import dev.kast.util.Keys;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings of one module, built once from module.settings (+ Bind and "Show in list").
 * Row geometry is written during render() and reused for hit-testing, so what you
 * click is exactly what was drawn.
 */
public final class ModuleSettingsPanel {
	private enum T { BOOL, NUM, MODE, COLOR, BIND, LIST }

	private static final class R {
		final T type;
		final Setting s;
		final Tween drop = new Tween(0.22f, false);
		final Anim sw = new Anim(0);
		float x, y, w, h;
		boolean visible = true;

		R(T type, Setting s) {
			this.type = type;
			this.s = s;
		}
	}

	private static final float GAP = 3f, OPT_H = 13f;

	private final Module m;
	private final ClickGuiScreen owner;
	private final List<R> rows = new ArrayList<>();
	private float contentH;
	private R dragging;
	private float ax, ay, aw, ah;

	public ModuleSettingsPanel(Module m, ClickGuiScreen owner) {
		this.m = m;
		this.owner = owner;
		for (Setting s : m.settings) {
			T t = s instanceof BoolSetting ? T.BOOL
				: s instanceof NumSetting ? T.NUM
				: s instanceof ModeSetting ? T.MODE
				: T.COLOR;
			R r = new R(t, s);
			if (s instanceof BoolSetting b) r.sw.snap(b.value ? 1f : 0f);
			rows.add(r);
		}
		if (!m.locked) {
			rows.add(new R(T.BIND, null));
			R l = new R(T.LIST, null);
			l.sw.snap(m.showInList ? 1f : 0f);
			rows.add(l);
		}
	}

	public boolean isEmpty() {
		return rows.isEmpty();
	}

	public boolean isDragging() {
		return dragging != null;
	}

	public float contentHeight() {
		return contentH;
	}

	private float height(R r) {
		return switch (r.type) {
			case NUM, COLOR -> 25f;
			case MODE -> 16f + r.drop.out() * ((ModeSetting) r.s).modes.length * OPT_H;
			default -> 16f;
		};
	}

	/** Advances dropdown animations and recomputes the content height (once per frame). */
	public void update(float dt) {
		float total = 3f;
		for (R r : rows) {
			r.visible = r.s == null || r.s.visible.getAsBoolean();
			if (!r.visible) {
				r.drop.snap(false);
				continue;
			}
			r.drop.update(dt);
			total += height(r) + GAP;
		}
		contentH = total;
	}

	public boolean contains(double mx, double my) {
		return Ui.hit(mx, my, ax, ay, aw, ah);
	}

	/* ---------------------------------------------------------------- render */

	public void render(Gfx g, float x, float y, float w, float visibleH, int mx, int my) {
		ax = x;
		ay = y;
		aw = w;
		ah = visibleH;
		int accent = Theme.accent(y);
		float cy = y + 3f;
		for (R r : rows) {
			if (!r.visible) continue;
			r.x = x + 6f;
			r.w = w - 12f;
			r.y = cy;
			r.h = height(r);
			cy += r.h + GAP;
			switch (r.type) {
				case BOOL -> {
					BoolSetting b = (BoolSetting) r.s;
					g.text(b.name, r.x, Ui.ty(r.y, 16), Theme.TEXT);
					GuiRenderer.toggle(g, r.x + r.w - 20, r.y + 3, r.sw.set(b.value ? 1f : 0f).get(16f), accent);
				}
				case LIST -> {
					g.text("Show in list", r.x, Ui.ty(r.y, 16), Theme.TEXT);
					GuiRenderer.toggle(g, r.x + r.w - 20, r.y + 3, r.sw.set(m.showInList ? 1f : 0f).get(16f), accent);
				}
				case BIND -> {
					g.text("Bind", r.x, Ui.ty(r.y, 16), Theme.TEXT);
					boolean listening = owner.isBinding(m);
					String kn = listening ? "..." : (m.key > 0 ? Keys.name(m.key) : "NONE");
					float kw = g.width(kn) + 12;
					GuiRenderer.chip(g, r.x + r.w - kw, r.y + 1, kw, 14, listening ? Col.alpha(accent, 0x90) : 0xFF22263A);
					g.text(kn, r.x + r.w - kw + 6, Ui.ty(r.y + 1, 14), Theme.TEXT);
				}
				case NUM -> {
					NumSetting n = (NumSetting) r.s;
					g.text(n.name, r.x, r.y + 1, Theme.TEXT);
					String v = n.display();
					g.text(v, r.x + r.w - g.width(v), r.y + 1, Theme.TEXT_DIM);
					float f = (float) ((n.value - n.min) / (n.max - n.min));
					float by = r.y + 15;
					g.round(r.x, by, r.w, 3, 1.5f, 0xFF2A2E40);
					g.round(r.x, by, Math.max(3f, r.w * f), 3, 1.5f, accent);
					g.circle(r.x + r.w * f, by + 1.5f, 3.6f, 0xFFFFFFFF);
				}
				case COLOR -> {
					ColorSetting c = (ColorSetting) r.s;
					g.text(c.name, r.x, r.y + 1, Theme.TEXT);
					g.circle(r.x + r.w - 4, r.y + 4.5f, 4f, c.get());
					float by = r.y + 15;
					int seg = 28;
					for (int i = 0; i < seg; i++) {
						g.rect(r.x + r.w * i / seg, by, r.w / seg + 0.6f, 3.5f, Col.hsb(i / (float) (seg - 1), c.sat, c.bri));
					}
					g.circle(r.x + r.w * c.hue, by + 1.7f, 3.6f, 0xFFFFFFFF);
					g.circle(r.x + r.w * c.hue, by + 1.7f, 2.2f, c.get());
				}
				case MODE -> {
					ModeSetting ms = (ModeSetting) r.s;
					float de = r.drop.out();
					g.text(ms.name, r.x, Ui.ty(r.y, 16), Theme.TEXT);
					String v = ms.get();
					float kw = g.width(v) + 22;
					GuiRenderer.chip(g, r.x + r.w - kw, r.y + 1, kw, 14, Col.alpha(accent, 0x50));
					g.text(v, r.x + r.w - kw + 6, Ui.ty(r.y + 1, 14), 0xFFFFFFFF);
					GuiRenderer.chevron(g, r.x + r.w - 9, r.y + 8, r.drop.inOut(), 0xFFFFFFFF);
					if (r.drop.linear() > 0.01f) {
						float a0 = g.alpha;
						g.alpha = a0 * de;
						for (int i = 0; i < ms.modes.length; i++) {
							float oy = r.y + 16 + i * OPT_H * de;
							boolean sel = i == ms.index;
							boolean hov = Ui.hit(mx, my, r.x, oy, r.w, OPT_H - 1);
							if (sel) g.round(r.x, oy, r.w, OPT_H - 1, 4, Col.alpha(accent, 0x48));
							else if (hov) g.round(r.x, oy, r.w, OPT_H - 1, 4, 0x20FFFFFF);
							g.text(ms.modes[i], r.x + 6, Ui.ty(oy, OPT_H - 1), sel ? 0xFFFFFFFF : Theme.TEXT_DIM, 0.95f);
						}
						g.alpha = a0;
					}
				}
			}
		}
	}

	/* ----------------------------------------------------------------- input */

	public boolean click(double mx, double my, int b) {
		for (R r : rows) {
			if (!r.visible || my < r.y || my >= r.y + r.h + GAP * 0.5f) continue;
			switch (r.type) {
				case BOOL -> {
					BoolSetting bs = (BoolSetting) r.s;
					bs.value = !bs.value;
				}
				case LIST -> m.showInList = !m.showInList;
				case BIND -> owner.beginBind(m);
				case NUM, COLOR -> {
					dragging = r;
					drag(mx);
				}
				case MODE -> {
					ModeSetting ms = (ModeSetting) r.s;
					if (my < r.y + 16) {
						if (b == 1 && !r.drop.target()) ms.cycle(-1);
						else r.drop.set(!r.drop.target());
					} else if (r.drop.target()) {
						int i = (int) ((my - (r.y + 16)) / OPT_H);
						if (i >= 0 && i < ms.modes.length) {
							ms.index = i;
							r.drop.set(false);
						}
					}
				}
			}
			return true;
		}
		return true;
	}

	public void drag(double mx) {
		if (dragging == null) return;
		double f = Math.max(0.0, Math.min(1.0, (mx - dragging.x) / dragging.w));
		if (dragging.s instanceof NumSetting n) n.set(n.min + f * (n.max - n.min));
		else if (dragging.s instanceof ColorSetting c) c.hue = (float) f;
	}

	public void release() {
		dragging = null;
	}
}
