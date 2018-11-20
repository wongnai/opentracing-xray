package com.wongnai.common;

import java.net.URL;

/**
 * Utilities for working with resources.
 *
 * @author Suparit Krityakien
 */
public final class ResourceUtils {
	private ResourceUtils() {
	}

	/**
	 * Gets URI of resource.
	 *
	 * @param resourceName
	 *            resource name from context (root) e.g.
	 *            com/wongnai/domain/resource.txt
	 * @return url
	 */
	public static URL getURL(String resourceName) {
		return Thread.currentThread().getContextClassLoader().getResource(resourceName);
	}
}
