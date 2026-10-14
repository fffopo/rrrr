package dev.kast.render;

import dev.kast.util.Compat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * World-to-screen projection done by hand (camera position, yaw/pitch, FOV),
 * so the ESP/Tracers can be drawn in the 2D HUD pass without touching the 3D
 * render pipeline. First-person only.
 */
public final class Proj {
	private static double ex, ey, ez;
	private static double fx, fy, fz, rx, rz, ux, uy, uz;
	private static double tanHalf, aspect;
	private static float sw, sh;
	private static boolean ready;

	private Proj() {}

	public static void update(float pt) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			ready = false;
			return;
		}
		Vec3 eye = mc.player.getEyePosition(pt);
		ex = eye.x;
		ey = eye.y;
		ez = eye.z;
		double yaw = Math.toRadians(Compat.yaw(mc.player, pt));
		double pitch = Math.toRadians(Compat.pitch(mc.player, pt));
		double sy = Math.sin(yaw), cy = Math.cos(yaw), sp = Math.sin(pitch), cp = Math.cos(pitch);
		fx = -sy * cp;
		fy = -sp;
		fz = cy * cp;
		rx = -cy;
		rz = -sy;
		ux = -sy * sp;
		uy = cp;
		uz = cy * sp;
		double fov = mc.options.fov().get();
		tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
		sw = mc.getWindow().getGuiScaledWidth();
		sh = mc.getWindow().getGuiScaledHeight();
		aspect = sw / (double) sh;
		ready = true;
	}

	/** @return {screenX, screenY} in GUI units, or null if behind the camera. */
	public static float[] project(double x, double y, double z) {
		if (!ready) return null;
		double dx = x - ex, dy = y - ey, dz = z - ez;
		double zc = dx * fx + dy * fy + dz * fz;
		if (zc < 0.05) return null;
		double xc = dx * rx + dz * rz;
		double yc = dx * ux + dy * uy + dz * uz;
		float px = (float) (sw / 2.0 * (1.0 + xc / (zc * tanHalf * aspect)));
		float py = (float) (sh / 2.0 * (1.0 - yc / (zc * tanHalf)));
		return new float[] {px, py};
	}
}
