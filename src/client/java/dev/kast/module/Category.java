package dev.kast.module;

public enum Category {
	COMBAT("Combat"),
	MOVEMENT("Movement"),
	VISUALS("Visuals"),
	PLAYER("Player"),
	MISC("Miscellaneous");

	public final String title;

	Category(String title) {
		this.title = title;
	}
}
