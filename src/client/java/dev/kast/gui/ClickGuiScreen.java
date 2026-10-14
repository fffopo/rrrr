package dev.kast.gui;

import dev.kast.config.ConfigManager;
import dev.kast.module.Category;
import dev.kast.module.Module;
import dev.kast.render.AnimationController;
import dev.kast.render.Col;
import dev.kast.render.Gfx;
import dev.kast.render.GuiRenderer;
import dev.kast.render.Theme;
import dev.kast.render.Tween;
import dev.kast.util.Compat;
import dev.kast.util.Keys;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Click GUI: five category panels, search at the bottom.
 * LMB toggles a module, RMB opens/closes its settings, MMB binds a key.
 * Typing goes to the search field (config name field while Configs is open).
 */
public final class ClickGuiScreen extends Screen {
	private final List<CategoryPanel> panels = new ArrayList<>();
	private final Tween openT = new Tween(0.28f, false);
	private final Tween cfgT = new Tween(0.22f, false);
	private final Tween closeT = new Tween(0.18f, true);
	private boolean closing;

	private String search = "";
	private String cfgName = "";
	private boolean cfgOpen;
	private Module bindModule;
	private CategoryPanel dragPanel;
	private ModuleSettingsPanel activeSettings;
	private String toast = "";
	private long toastUntil;

	private float sbX, sbY, sbW, sbH;
	private float cfgBtnX, cfgBtnW;
	private float cfgX, cfgY, cfgW, cfgH;

	public ClickGuiScreen() {
		super(Component.literal("KastDLC"));
		for (Category c : Category.values()) panels.add(new CategoryPanel(this, c));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	/* --------------------------------------------------------- shared state */

	boolean canHover() {
		return !cfgOpen && dragPanel == null;
	}

	boolean isBinding(Module m) {
		return bindModule == m;
	}

	void beginBind(Module m) {
		bindModule = m;
	}

	void setDragPanel(CategoryPanel p) {
		dragPanel = p;
		// bring to front
		panels.remove(p);
		panels.add(p);
	}

	void setActiveSettings(ModuleSettingsPanel p) {
		activeSettings = p;
	}

	void notify(String s) {
		toast = s;
		toastUntil = System.currentTimeMillis() + 1800;
	}

	private String query() {
		return search.trim().toLowerCase(Locale.ROOT);
	}

	/* ---------------------------------------------------------------- close */

	private void startClose() {
		if (closing) return;
		closing = true;
		closeT.set(false);
		releaseAll();
	}

	private void releaseAll() {
		dragPanel = null;
		if (activeSettings != null) activeSettings.release();
		activeSettings = null;
		bindModule = null;
	}

	@Override
	public void onClose() {
		ConfigManager.save("default");
		super.onClose();
	}

	@Override
	public void removed() {
		releaseAll();
		super.removed();
	}

	/* --------------------------------------------------------------- layout */

	private void layout() {
		float gap = 6f;
		float pw = Ui.clamp((width - 24f - 4 * gap) / 5f, 84f, 124f);
		float ph = Ui.clamp(height - 78f, 150f, 250f);
		float total = pw * 5 + gap * 4;
		float x0 = (width - total) / 2f;
		float y0 = (height - ph - 46f) / 2f;
		// list order changes when a panel is dragged to the front, so place by category index
		for (CategoryPanel p : panels) {
			p.place(x0 + p.index() * (pw + gap), y0, pw, ph);
		}
		sbW = 140f;
		sbH = 18f;
		sbX = (width - sbW) / 2f - 24f;
		sbY = y0 + ph + 10f;
		cfgBtnW = 48f;
		cfgBtnX = sbX + sbW + 6f;
		cfgW = 170f;
		cfgH = 130f;
		cfgX = (width - cfgW) / 2f;
		cfgY = sbY - cfgH - 6f;
	}

	/* --------------------------------------------------------------- render */

	@Override
	public void extractRenderState(GuiGraphicsExtractor gg, int mx, int my, float delta) {
		AnimationController.beginFrame();
		float dt = AnimationController.dt();

		// lost mouse focus / release outside the window: drop any drag
		if ((dragPanel != null || activeSettings != null) && !Compat.mouseDown(0)) releaseAll();

		openT.set(true);
		openT.update(dt);
		closeT.update(dt);
		if (closing && closeT.linear() <= 0f) {
			onClose();
			return;
		}
		float open = openT.out() * closeT.out();
		cfgT.set(cfgOpen);
		cfgT.update(dt);

		layout();
		String q = query();
		for (CategoryPanel p : panels) p.update(dt, q);

		Gfx g = new Gfx(gg);
		g.alpha = open;
		g.rect(0, 0, width, height, 0x70000000);

		for (CategoryPanel p : panels) p.render(g, mx, my, open);

		drawSearch(g, mx, my);
		drawHint(g);
		drawConfigs(g, mx, my);
		g.alpha = 1f;
	}

	private void drawSearch(Gfx g, int mx, int my) {
		int accent = Theme.accent(0);
		g.shadow(sbX, sbY, sbW, sbH, 7, 8, 0x90000000);
		g.round(sbX, sbY, sbW, sbH, 7, 0xD2080B16);
		boolean typing = !cfgOpen;
		String t = search;
		boolean blink = typing && (System.currentTimeMillis() / 500) % 2 == 0;
		if (t.isEmpty()) {
			g.text(cfgOpen ? "Поиск" : "Поиск", sbX + 10, Ui.ty(sbY, sbH), Theme.TEXT_DIM);
			if (blink) g.rect(sbX + 10, sbY + 4.5f, 1, 9, 0xFFFFFFFF);
		} else {
			float maxW = sbW - 34f;
			String shown = t;
			while (g.width(shown) > maxW && shown.length() > 1) shown = shown.substring(1);
			g.text(shown, sbX + 10, Ui.ty(sbY, sbH), Theme.TEXT);
			if (blink) g.rect(sbX + 10 + g.width(shown) + 1, sbY + 4.5f, 1, 9, 0xFFFFFFFF);
			boolean hov = Ui.hit(mx, my, sbX + sbW - 18, sbY, 18, sbH);
			GuiRenderer.cross(g, sbX + sbW - 10, sbY + sbH / 2f, 2.6f, hov ? 0xFFFFFFFF : Theme.TEXT_DIM);
		}

		boolean hb = Ui.hit(mx, my, cfgBtnX, sbY, cfgBtnW, sbH);
		g.shadow(cfgBtnX, sbY, cfgBtnW, sbH, 7, 8, 0x90000000);
		g.round(cfgBtnX, sbY, cfgBtnW, sbH, 7, cfgOpen ? Col.alpha(accent, 0xC0) : (hb ? 0xE0151A2C : 0xD2080B16));
		g.textCentered("Configs", cfgBtnX + cfgBtnW / 2f, Ui.ty(sbY, sbH), 0xFFFFFFFF, 0.95f);
	}

	private void drawHint(Gfx g) {
		String hint;
		if (bindModule != null) {
			hint = "Нажми клавишу для " + bindModule.name + "  (Backspace - убрать, Esc - отмена)";
		} else if (System.currentTimeMillis() < toastUntil) {
			hint = toast;
		} else {
			hint = "ЛКМ - вкл/выкл  |  ПКМ - настройки  |  СКМ - бинд  |  " + Keys.name(ConfigManager.guiKey) + " - закрыть";
		}
		g.textCentered(hint, width / 2f, sbY + sbH + 8f, Theme.TEXT_DIM, 0.9f);
	}

	private void drawConfigs(Gfx g, int mx, int my) {
		float a = cfgT.out();
		if (a <= 0.01f) return;
		float a0 = g.alpha;
		g.alpha = a0 * a;
		float y = cfgY + (1f - a) * 8f;
		int accent = Theme.accent(0);
		GuiRenderer.panel(g, cfgX, y, cfgW, cfgH, 9);
		g.text("Configs", cfgX + 10, y + 8, 0xFFFFFFFF, 1.1f);

		// name field + save
		float fy = y + 22;
		g.round(cfgX + 8, fy, cfgW - 62, 16, 5, 0xB0050810);
		boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
		if (cfgName.isEmpty()) g.text("Имя конфига", cfgX + 14, Ui.ty(fy, 16), Theme.TEXT_DIM, 0.95f);
		else g.text(cfgName + (blink ? "_" : ""), cfgX + 14, Ui.ty(fy, 16), Theme.TEXT, 0.95f);
		boolean hs = Ui.hit(mx, my, cfgX + cfgW - 50, fy, 42, 16);
		g.round(cfgX + cfgW - 50, fy, 42, 16, 5, hs ? accent : Col.alpha(accent, 0xB0));
		g.textCentered("Save", cfgX + cfgW - 29, Ui.ty(fy, 16), 0xFFFFFFFF, 0.95f);

		List<String> names = ConfigManager.list();
		float ly = fy + 22;
		g.scissor(cfgX + 4, ly, cfgW - 8, cfgH - 48);
		if (names.isEmpty()) g.textCentered("Пока нет конфигов", cfgX + cfgW / 2f, ly + 14, Theme.TEXT_DIM, 0.9f);
		for (int i = 0; i < names.size() && i < 5; i++) {
			float ry = ly + i * 17;
			g.round(cfgX + 8, ry, cfgW - 16, 15, 5, 0x90070A12);
			g.text(names.get(i), cfgX + 14, Ui.ty(ry, 15), Theme.TEXT, 0.95f);
			boolean hl = Ui.hit(mx, my, cfgX + cfgW - 62, ry + 1, 28, 13);
			boolean hd = Ui.hit(mx, my, cfgX + cfgW - 32, ry + 1, 24, 13);
			g.round(cfgX + cfgW - 62, ry + 1, 28, 13, 4, hl ? accent : Col.alpha(accent, 0x70));
			g.textCentered("Load", cfgX + cfgW - 48, Ui.ty(ry + 1, 13), 0xFFFFFFFF, 0.8f);
			g.round(cfgX + cfgW - 32, ry + 1, 24, 13, 4, hd ? 0xFFE0524F : 0x90E0524F);
			g.textCentered("Del", cfgX + cfgW - 20, Ui.ty(ry + 1, 13), 0xFFFFFFFF, 0.8f);
		}
		g.unscissor();
		g.alpha = a0;
	}

	/* ---------------------------------------------------------------- input */

	@Override
	public boolean mouseClicked(MouseButtonEvent e, boolean dbl) {
		double mx = e.x(), my = e.y();
		int b = e.button();
		if (closing) return true;

		if (bindModule != null) {
			bindModule = null;
			return true;
		}

		// configs popup swallows everything inside it
		if (cfgOpen && Ui.hit(mx, my, cfgX, cfgY, cfgW, cfgH)) {
			clickConfigs(mx, my);
			return true;
		}
		if (Ui.hit(mx, my, cfgBtnX, sbY, cfgBtnW, sbH)) {
			cfgOpen = !cfgOpen;
			return true;
		}
		if (cfgOpen) {
			cfgOpen = false;
			return true;
		}

		if (!search.isEmpty() && Ui.hit(mx, my, sbX + sbW - 18, sbY, 18, sbH)) {
			search = "";
			return true;
		}

		// topmost panel first (panels list is ordered back to front)
		for (int i = panels.size() - 1; i >= 0; i--) {
			CategoryPanel p = panels.get(i);
			if (p.contains(mx, my)) {
				p.mouseClicked(mx, my, b);
				return true;
			}
		}
		return true;
	}

	private void clickConfigs(double mx, double my) {
		float fy = cfgY + 22;
		if (Ui.hit(mx, my, cfgX + cfgW - 50, fy, 42, 16)) {
			saveConfig();
			return;
		}
		List<String> names = ConfigManager.list();
		float ly = fy + 22;
		for (int i = 0; i < names.size() && i < 5; i++) {
			float ry = ly + i * 17;
			if (Ui.hit(mx, my, cfgX + cfgW - 62, ry + 1, 28, 13)) {
				notify(ConfigManager.load(names.get(i)) ? "Загружен " + names.get(i) : "Не удалось загрузить");
				return;
			}
			if (Ui.hit(mx, my, cfgX + cfgW - 32, ry + 1, 24, 13)) {
				ConfigManager.delete(names.get(i));
				notify("Удалён " + names.get(i));
				return;
			}
		}
	}

	private void saveConfig() {
		String n = ConfigManager.clean(cfgName);
		if (n.isEmpty()) {
			notify("Введи имя конфига");
			return;
		}
		notify(ConfigManager.save(n) ? "Сохранён " + n : "Не удалось сохранить");
		cfgName = "";
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
		if (dragPanel != null) {
			dragPanel.dragTo(e.x(), e.y(), width, height);
			return true;
		}
		if (activeSettings != null) {
			activeSettings.drag(e.x());
			return true;
		}
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent e) {
		releaseAll();
		return true;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double hx, double vy) {
		if (cfgOpen) return true;
		for (int i = panels.size() - 1; i >= 0; i--) {
			CategoryPanel p = panels.get(i);
			if (p.contains(mx, my)) {
				p.scroll(vy);
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent e) {
		int key = e.key();

		if (bindModule != null) {
			if (key == 256) bindModule = null;
			else if (key == 259 || key == 261) {
				bindModule.key = -1;
				bindModule = null;
			} else {
				bindModule.key = key;
				bindModule = null;
			}
			return true;
		}

		if (key == 256) { // ESC
			if (cfgOpen) cfgOpen = false;
			else startClose();
			return true;
		}
		if (key == ConfigManager.guiKey && (cfgOpen ? cfgName.isEmpty() : search.isEmpty())) {
			startClose();
			return true;
		}
		if (key == 259) { // BACKSPACE
			if (cfgOpen) {
				if (!cfgName.isEmpty()) cfgName = cfgName.substring(0, cfgName.length() - 1);
			} else if (!search.isEmpty()) {
				search = search.substring(0, search.length() - 1);
			}
			return true;
		}
		if (key == 257 || key == 335) { // ENTER
			if (cfgOpen) saveConfig();
			else jumpToFirstMatch();
			return true;
		}
		return true; // never let keys reach the game binds while the GUI is open
	}

	private void jumpToFirstMatch() {
		String q = query();
		if (q.isEmpty()) return;
		for (CategoryPanel p : panels) {
			ModuleEntry m = p.firstMatch(q);
			if (m != null) {
				p.scrollTo(m);
				m.flash();
				return;
			}
		}
		notify("Ничего не найдено");
	}

	@Override
	public boolean charTyped(CharacterEvent e) {
		if (bindModule != null) return true;
		int cp = e.codepoint();
		if (cp < 32 || cp == 127) return true;
		if (cfgOpen) {
			if (cfgName.length() < 24 && cp < 128 && String.valueOf((char) cp).matches("[a-zA-Z0-9_\\-]")) {
				cfgName += (char) cp;
			}
		} else if (search.length() < 24) {
			search += new String(Character.toChars(cp));
		}
		return true;
	}
}
