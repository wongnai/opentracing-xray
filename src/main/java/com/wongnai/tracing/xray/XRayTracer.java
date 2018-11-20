package com.wongnai.tracing.xray;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.xray.AWSXRayRecorder;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

/**
 * An implementation of {@link Tracer} for AWS X-Ray.
 *
 * @author Suparit Krityakien
 */
public class XRayTracer extends XRayActiveSpanSource implements Tracer {
	private Map<Format<?>, XRaySpanContextConverter> converters;

	/**
	 * Constructs an instance.
	 *
	 * @param recorder
	 *            recorder
	 */
	public XRayTracer(AWSXRayRecorder recorder) {
		super(recorder);

		converters = new HashMap<>();
		HttpSpanContextConverter httpSpanContextConverter = new HttpSpanContextConverter();
		converters.put(Format.Builtin.TEXT_MAP, httpSpanContextConverter);
		converters.put(Format.Builtin.HTTP_HEADERS, httpSpanContextConverter);
	}

	@Override
	public SpanBuilder buildSpan(String s) {
		return new XRaySpanBuilder(s, this, getRecorder());
	}

	@Override
	public <C> void inject(SpanContext spanContext, Format<C> format, C c) {
		XRaySpanContextConverter converter = converters.get(format);
		if (converter != null && spanContext instanceof XRaySpanContext) {
			converter.inject((XRaySpanContext) spanContext, c);
		}
	}

	@Override
	public <C> SpanContext extract(Format<C> format, C c) {
		XRaySpanContextConverter converter = converters.get(format);
		if (converter != null) {
			return converter.extract(c);
		} else {
			return null;
		}
	}
}
