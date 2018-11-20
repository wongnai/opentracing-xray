package com.wongnai.tracing;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.propagation.TextMap;

public final class TextMapUtils {
	private TextMapUtils() {
	}

	/**
	 * Converts text map to map.
	 *
	 * @param textMap
	 *            text map
	 * @return map
	 */
	public static Map<String, String> toMap(TextMap textMap) {
		Map<String, String> map = new HashMap<>();
		textMap.forEach((i) -> map.put(i.getKey().toLowerCase(), i.getValue()));

		return map;
	}
}
