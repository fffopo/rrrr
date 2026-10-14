package dev.kast.gui;

import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.module.ModuleManager;
import dev.kast.render.Anim;
import dev.kast.render.Gfx;
import dev.kast.render.GuiRenderer;
import dev.kast.render.Theme;

import java.util.ArrayList;
import java.util.List;

/** One vertical panel: title, own scroll area and the module rows of a category. */
public final class CategoryPanel {
	static final float HEADER = 24f, ROW_H = 16f, GAP = 2f, PAD = 5f;

	private final ClickGuiScreen owner;
	private final Category cat;
	private final List<ModuleEntry> entries = new ArrayList<>();

	private float baseX, baseY, w, h;
	private float dx, dy;
	private float x, y;
	private float scrollT;
	private final Anim scrollA = new Anim(0);
	private float contentH, viewTop, viewH;
	private float grabX, grabY;

	public CategoryPanel(ClickGuiScreen owner, Category cat) {
		this.owner = owner;
		this.cat = cat;
		for (Module m : ModuleManager.in(cat)) entries.add(new ModuleEntry(m, owner));
	}

	/** Column index (order of Category). */
	public int index() {
		return cat.ordinal();
	}

	public void place(float x, float y, float w, float h) {
		this.baseX = x;
		this.baseY = y;
		this.w = w;
		this.h = h;
	}

	public float bottom() {
		return baseY + h;
	}

	public boolean contains(double mx, double my) {
		return Ui.hit(mx, my, x, y, w, h);
	}

	private float maxScroll() {
		return Math.max(0f, contentH - viewH + 2f);
	}

	public void update(float dt, String query) {
		float total = 0f;
		boolean empty = query.isEmpty();
		for (ModuleEntry e : entries) {
			e.update(dt, empty || e.matches(query));
			total += e.height();
		}
		contentH = total;
		viewH = h - HEADER - 3f - PAD;
		scrollT = Ui.clamp(scrollT, 0f, maxScroll());
	}

	public void render(Gfx g, int mx, int my, float open) {
		x = baseX + dx;
		y = baseY + dy + (1f - open) * 16f;
		int accent = Theme.accent(x);

		GuiRenderer.panel(g, x, y, w, h, 9);
		g.textCentered(cat.title, x + w / 2f, y + 7.5f, 0xFFFFFFFF, 1.2f);
		g.rect(x + 10, y + HEADER - 1f, w - 20, 1, 0x16FFFFFF);

		viewTop = y + HEADER + 3f;
		boolean inList = owner.canHover() && Ui.hit(mx, my, x, viewTop, w, viewH);
		float scroll = scrollA.set(scrollT).get(16f);

		g.scissor(x + 2, viewTop, w - 4, viewH);
		if (entries.isEmpty()) {
			g.textCentered("Нет модулей", x + w / 2f, viewTop + 14, Theme.TEXT_DIM, 0.9f);
		}
		float cy = viewTop - scroll;
		for (ModuleEntry e : entries) {
			float eh = e.height();
			if (eh > 0.01f && cy + eh > viewTop - 2f && cy < viewTop + viewH + 2f) {
				e.render(g, x + PAD, cy, w - 2 * PAD, mx, my, inList, accent);
			} else {
				e.markHidden();
			}
			cy += eh;
		}
		g.unscissor();
		GuiRenderer.scrollbar(g, x + w - 4f, viewTop, viewH, scroll, contentH);
	}

	public void mouseClicked(double mx, double my, int b) {
		if (my < y + HEADER) {
			if (b == 0) {
				grabX = (float) (mx - x);
				grabY = (float) (my - y);
				owner.setDragPanel(this);
			}
			return;
		}
		if (my < viewTop || my >= viewTop + viewH) return;
		for (ModuleEntry e : entries) {
			if (e.click(mx, my, b, owner)) return;
		}
	}

	public void dragTo(double mx, double my, int sw, int sh) {
		float nx = Ui.clamp((float) mx - grabX, 4f, sw - w - 4f);
		float ny = Ui.clamp((float) my - grabY, 4f, sh - HEADER - 4f);
		dx = nx - baseX;
		dy = ny - baseY;
	}

	public void scroll(double amount) {
		scrollT = Ui.clamp(scrollT - (float) amount * 22f, 0f, maxScroll());
	}

	/** First entry that matches the query, or null. */
	public ModuleEntry firstMatch(String q) {
		for (ModuleEntry e : entries) if (e.matches(q)) return e;
		return null;
	}

	public void scrollTo(ModuleEntry target) {
		float off = 0f;
		for (ModuleEntry e : entries) {
			if (e == target) break;
			off += e.height();
		}
		scrollT = Ui.clamp(off - 4f, 0f, maxScroll());
	}
}
