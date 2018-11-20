package com.wongnai.tracing.xray;

/**
 * Converter to convert span context to/from carrier.
 *
 * @author Suparit Krityakien
 */
public interface XRaySpanContextConverter {
	/**
	 * Injects span context's data to carrier.
	 *
	 * @param spanContext
	 *            span context
	 * @param carrier
	 *            carrier
	 */
	void inject(XRaySpanContext spanContext, Object carrier);

	/**
	 * Extracts span context from carrier.
	 *
	 * @param carrier
	 *            carrier
	 * @return span context
	 */
	XRaySpanContext extract(Object carrier);
}
