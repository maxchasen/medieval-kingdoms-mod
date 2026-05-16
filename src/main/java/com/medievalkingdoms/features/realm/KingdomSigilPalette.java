package com.medievalkingdoms.features.realm;

/** Banner-style kingdom colors (ARGB). Client UI and server validation share this list. */
public final class KingdomSigilPalette {
	public static final int DEFAULT_COLOR = 0xFFD4AF37;

	/** Distinct RGBs (alpha applied on send). */
	public static final int[] COLORS = {
			0xFFE6194B,
			0xFF3CB44B,
			0xFFFFE119,
			0xFF4363D8,
			0xFFF58231,
			0xFF911EB4,
			0xFF42D4F4,
			0xFFF032E6,
			0xFFBFEF45,
			0xFFFABED4,
			0xFF469990,
			0xFFDCBEFF,
			0xFF9A6324,
			0xFFFFFF68,
			0xFF800000,
			0xFFAAFFC3,
	};

	private KingdomSigilPalette() {
	}

	/** Normalize to full ARGB and match an approved swatch (RGB compared). */
	public static int sanitize(int argbOrRgb) {
		int rgb = argbOrRgb & 0xFFFFFF;
		int full = 0xFF000000 | rgb;
		for (int c : COLORS) {
			if ((c & 0xFFFFFF) == rgb) {
				return c;
			}
		}
		if (rgb == (DEFAULT_COLOR & 0xFFFFFF)) {
			return DEFAULT_COLOR;
		}
		return DEFAULT_COLOR;
	}
}
