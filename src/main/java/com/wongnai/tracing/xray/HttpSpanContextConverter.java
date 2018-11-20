package com.wongnai.tracing.xray;

import java.util.Map;

import com.amazonaws.xray.entities.TraceHeader;
import com.wongnai.common.StringUtils;
import com.wongnai.tracing.TextMapUtils;

import io.opentracing.propagation.TextMap;

/**
 * Converts span context to/from http carrier.
 *
 * @author Suparit Krityakien
 */
public class HttpSpanContextConverter implements XRaySpanContextConverter {
	private static final String TRACE_ID_HEADER = "X-Amzn-Trace-Id".toLowerCase();

	@Override
	public void inject(XRaySpanContext spanContext, Object carrier) {
		TextMap tm = (TextMap) carrier;

		TraceHeader.SampleDecision d = spanContext.getSampleDecision();
		if (d == null) {
			d = TraceHeader.SampleDecision.UNKNOWN;
		}

		TraceHeader header = new TraceHeader(spanContext.getTraceId(),
				d == TraceHeader.SampleDecision.SAMPLED ? spanContext.getId() : null, d);

		tm.put(TRACE_ID_HEADER, header.toString());
	}

	@Override
	public XRaySpanContext extract(Object carrier) {
		TextMap tm = (TextMap) carrier;

		Map<String, String> map = TextMapUtils.toMap(tm);

		String id = map.get(TRACE_ID_HEADER);
		if (!StringUtils.isBlank(id)) {
			return XRaySpanContext.create(TraceHeader.fromString(id));
		} else {
			return null;
		}
	}
}
