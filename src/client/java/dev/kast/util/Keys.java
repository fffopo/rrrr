package dev.kast.util;

public final class Keys {
	private Keys() {}

	public static String name(int k) {
		if (k <= 0) return "NONE";
		if ((k >= 65 && k <= 90) || (k >= 48 && k <= 57)) return String.valueOf((char) k);
		if (k >= 290 && k <= 314) return "F" + (k - 289);
		return switch (k) {
			case 32 -> "SPACE";
			case 340 -> "LSHIFT";
			case 344 -> "RSHIFT";
			case 341 -> "LCTRL";
			case 345 -> "RCTRL";
			case 342 -> "LALT";
			case 346 -> "RALT";
			case 258 -> "TAB";
			case 259 -> "BKSP";
			case 257 -> "ENTER";
			case 262 -> "RIGHT";
			case 263 -> "LEFT";
			case 264 -> "DOWN";
			case 265 -> "UP";
			case 280 -> "CAPS";
			case 45 -> "-";
			case 61 -> "=";
			case 96 -> "`";
			case 91 -> "[";
			case 93 -> "]";
			case 59 -> ";";
			case 39 -> "'";
			case 44 -> ",";
			case 46 -> ".";
			case 47 -> "/";
			case 92 -> "\\";
			default -> "K" + k;
		};
	}
}
