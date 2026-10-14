package dev.kast.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Custom 2D renderer on top of GuiGraphics.
 *
 * Shapes are drawn in PHYSICAL pixels (the GUI scale is cancelled out), and the
 * edges of rounded shapes are anti-aliased with fractional coverage, so there are
 * no stair-steps even at GUI scale 3-4. All public coordinates are in GUI units.
 *
 * Everything that touches GuiGraphics directly is in this file.
 */
public final class Gfx {
	public final GuiGraphicsExtractor g;
	private final Minecraft mc = Minecraft.getInstance();
	private final float s;
	/** Global alpha multiplier applied to everything drawn through this object. */
	public float alpha = 1f;

	public Gfx(GuiGraphicsExtractor g) {
		this.g = g;
		this.s = (float) mc.getWindow().getGuiScale();
	}

	public int width() {
		return mc.getWindow().getGuiScaledWidth();
	}

	public int height() {
		return mc.getWindow().getGuiScaledHeight();
	}

	private int c(int color) {
		return alpha >= 0.999f ? color : Col.mulAlpha(color, alpha);
	}

	private void begin() {
		g.pose().pushMatrix();
		g.pose().scale(1f / s, 1f / s);
	}

	private void end() {
		g.pose().popMatrix();
	}

	private void fillPx(int x1, int y1, int x2, int y2, int color) {
		if (x2 > x1 && y2 > y1 && (color >>> 24) != 0) g.fill(x1, y1, x2, y2, color);
	}

	/* ---------------------------------------------------------------- shapes */

	public void rect(float x, float y, float w, float h, int color) {
		begin();
		fillPx(Math.round(x * s), Math.round(y * s), Math.round((x + w) * s), Math.round((y + h) * s), c(color));
		end();
	}

	/** Anti-aliased rounded rectangle. */
	public void round(float x, float y, float w, float h, float r, int color) {
		begin();
		roundPx(x * s, y * s, w * s, h * s, r * s, c(color));
		end();
	}

	public void circle(float cx, float cy, float radius, int color) {
		round(cx - radius, cy - radius, radius * 2, radius * 2, radius, color);
	}

	private void roundPx(float X, float Y, float W, float H, float R, int color) {
		int ix = Math.round(X), iy = Math.round(Y), iw = Math.round(W), ih = Math.round(H);
		if (iw <= 0 || ih <= 0) return;
		R = Math.min(R, Math.min(iw, ih) / 2f);
		int rows = Math.min((int) Math.ceil(R), ih / 2);
		if (rows <= 0) {
			fillPx(ix, iy, ix + iw, iy + ih, color);
			return;
		}
		fillPx(ix, iy + rows, ix + iw, iy + ih - rows, color);
		int a = color >>> 24;
		for (int i = 0; i < rows; i++) {
			float dy = R - (i + 0.5f);
			float ext = (float) Math.sqrt(Math.max(0f, R * R - dy * dy));
			float inset = R - ext;
			float xl = ix + inset;
			float xr = ix + iw - inset;
			int il = (int) Math.ceil(xl);
			int ir = (int) Math.floor(xr);
			int yTop = iy + i;
			int yBot = iy + ih - 1 - i;
			fillPx(il, yTop, ir, yTop + 1, color);
			fillPx(il, yBot, ir, yBot + 1, color);
			float covL = il - xl;
			float covR = xr - ir;
			if (covL > 0.01f) {
				int ec = Col.alpha(color, Math.round(a * covL));
				fillPx(il - 1, yTop, il, yTop + 1, ec);
				fillPx(il - 1, yBot, il, yBot + 1, ec);
			}
			if (covR > 0.01f) {
				int ec = Col.alpha(color, Math.round(a * covR));
				fillPx(ir, yTop, ir + 1, yTop + 1, ec);
				fillPx(ir, yBot, ir + 1, yBot + 1, ec);
			}
		}
	}

	/** Soft shadow / glow around a rounded rectangle. */
	public void shadow(float x, float y, float w, float h, float r, float spread, int color) {
		int layers = 10;
		int base = color >>> 24;
		begin();
		for (int i = layers; i >= 1; i--) {
			float e = spread * i / layers;
			float f = 1f - (i - 0.5f) / layers;
			int a = Math.round(base * f * f * 0.35f);
			roundPx((x - e) * s, (y - e) * s, (w + e * 2) * s, (h + e * 2) * s, (r + e) * s, c(Col.alpha(color, a)));
		}
		end();
	}

	public void outline(float x, float y, float w, float h, float th, int color) {
		begin();
		int t = Math.max(1, Math.round(th * s));
		int X = Math.round(x * s), Y = Math.round(y * s), W = Math.round(w * s), H = Math.round(h * s);
		int col = c(color);
		fillPx(X, Y, X + W, Y + t, col);
		fillPx(X, Y + H - t, X + W, Y + H, col);
		fillPx(X, Y + t, X + t, Y + H - t, col);
		fillPx(X + W - t, Y + t, X + W, Y + H - t, col);
		end();
	}

	public void line(float x1, float y1, float x2, float y2, float th, int color) {
		float X1 = x1 * s, Y1 = y1 * s, X2 = x2 * s, Y2 = y2 * s;
		float dx = X2 - X1, dy = Y2 - Y1;
		float len = (float) Math.hypot(dx, dy);
		if (len < 0.5f || len > 6000f) return;
		int t = Math.max(1, Math.round(th * s));
		int n = Math.max(1, (int) Math.ceil(len / t));
		int col = c(color);
		begin();
		for (int i = 0; i <= n; i++) {
			float px = X1 + dx * i / n;
			float py = Y1 + dy * i / n;
			int ix = Math.round(px - t / 2f), iy = Math.round(py - t / 2f);
			fillPx(ix, iy, ix + t, iy + t, col);
		}
		end();
	}

	public void gradH(float x, float y, float w, float h, int c1, int c2, int steps) {
		float sw = w / steps;
		for (int i = 0; i < steps; i++) {
			rect(x + i * sw, y, sw + 0.6f, h, Col.lerp(c1, c2, i / (float) (steps - 1)));
		}
	}

	/* ------------------------------------------------------------------ text */

	public int width(String t) {
		return mc.font.width(t);
	}

	public float width(String t, float scale) {
		return mc.font.width(t) * scale;
	}

	public void text(String t, float x, float y, int color) {
		text(t, x, y, color, 1f);
	}

	public void text(String t, float x, float y, int color, float scale) {
		int col = c(color);
		if ((col >>> 24) < 4) return;
		g.pose().pushMatrix();
		g.pose().translate(x, y);
		if (scale != 1f) g.pose().scale(scale, scale);
		g.text(mc.font, t, 0, 0, col, false);
		g.pose().popMatrix();
	}

	public void textCentered(String t, float cx, float y, int color, float scale) {
		text(t, cx - width(t, scale) / 2f, y, color, scale);
	}

	/* --------------------------------------------------------------- scissor */

	public void scissor(float x, float y, float w, float h) {
		g.enableScissor(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h));
	}

	public void unscissor() {
		g.disableScissor();
	}
}
