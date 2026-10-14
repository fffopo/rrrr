package dev.kast.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

/**
 * All Minecraft calls that are likely to change between versions live here
 * (plus Gfx.java and the screen/hud signatures). If the build breaks after a
 * Minecraft update, fix it here first.
 */
public final class Compat {
	private Compat() {}

	public static long window() {
		return Minecraft.getInstance().getWindow().handle();
	}

	public static boolean keyDown(int key) {
		return key > 0 && GLFW.glfwGetKey(window(), key) == GLFW.GLFW_PRESS;
	}

	public static boolean mouseDown(int button) {
		return GLFW.glfwGetMouseButton(window(), button) == GLFW.GLFW_PRESS;
	}

	public static float yaw(Entity e, float pt) {
		return e.getViewYRot(pt);
	}

	public static float pitch(Entity e, float pt) {
		return e.getViewXRot(pt);
	}
}
