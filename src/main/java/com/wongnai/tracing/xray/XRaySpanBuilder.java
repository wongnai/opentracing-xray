package com.wongnai.tracing.xray;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.strategy.sampling.SamplingRequest;

import io.opentracing.ActiveSpan;
import io.opentracing.BaseSpan;
import io.opentracing.NoopSpan;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

/**
 * An implementation of {@link io.opentracing.Tracer.SpanBuilder} for AWS X-Ray.
 *
 * @author Suparit Krityakien
 */
public class XRaySpanBuilder implements Tracer.SpanBuilder {
	private Tracer tracer;
	private AWSXRayRecorder recorder;
	private String name;
	private SpanContext parentContext;
	private boolean ignoreActiveSpan;
	private Map<String, String> stringTags;
	private Map<String, Number> numberTags;
	private Map<String, Boolean> booleanTags;

	/**
	 * Constructs an instance.
	 *
	 * @param name
	 *            name
	 * @param tracer
	 *            tracer
	 * @param recorder
	 *            recorder
	 */
	public XRaySpanBuilder(String name, Tracer tracer, AWSXRayRecorder recorder) {
		this.name = name;
		this.tracer = tracer;
		this.recorder = recorder;
	}

	@Override
	public Tracer.SpanBuilder asChildOf(SpanContext sc) {
		this.parentContext = sc;

		return this;
	}

	@Override
	public Tracer.SpanBuilder asChildOf(BaseSpan<?> baseSpan) {
		this.parentContext = baseSpan.context();

		return this;
	}

	@Override
	public Tracer.SpanBuilder addReference(String s, SpanContext sc) {
		if (References.CHILD_OF.equals(s) || References.FOLLOWS_FROM.equals(s)) {
			this.parentContext = sc;
		}

		return this;
	}

	@Override
	public Tracer.SpanBuilder ignoreActiveSpan() {
		this.ignoreActiveSpan = true;

		return this;
	}

	@Override
	public Tracer.SpanBuilder withTag(String s, String s1) {
		if (stringTags == null) {
			stringTags = new HashMap<>();
		}
		stringTags.put(s, s1);

		return this;
	}

	@Override
	public Tracer.SpanBuilder withTag(String s, boolean b) {
		if (booleanTags == null) {
			booleanTags = new HashMap<>();
		}
		booleanTags.put(s, b);

		return this;
	}

	@Override
	public Tracer.SpanBuilder withTag(String s, Number number) {
		if (numberTags == null) {
			numberTags = new HashMap<>();
		}
		numberTags.put(s, number);

		return this;
	}

	@Override
	public Tracer.SpanBuilder withStartTimestamp(long l) {
		return this;
	}

	@Override
	public ActiveSpan startActive() {
		if (parentContext == null && !ignoreActiveSpan) {
			ActiveSpan parent = tracer.activeSpan();
			if (parent != null) {
				asChildOf(parent);
			}
		}
		return tracer.makeActive(startManual());
	}

	@Override
	public Span startManual() {
		if (parentContext == null || parentContext instanceof XRaySpanContext) {
			return startManualXray((XRaySpanContext) parentContext);
		} else {
			return NoopSpan.INSTANCE;
		}
	}

	private Span startManualXray(XRaySpanContext xrayParentContext) {
		Span span;

		if (xrayParentContext == null) {
			if (stringTags == null || !Tags.SPAN_KIND_SERVER.equals(stringTags.get(Tags.SPAN_KIND.getKey()))) {
				// Don't create a new subsegment to avoid xray's error/warning since we don't have any segment now.
				span = NoopSpan.INSTANCE;
			} else {
				span = createXraySpan(new XRaySpanContext());
				fillTags(span);
			}
		} else {
			Entity entity = xrayParentContext.getEntity();
			if (entity != null) {
				span = new XRaySpan.XRaySubsegmentSpan(recorder, recorder.beginSubsegment(name));
			} else {
				span = createXraySpan(xrayParentContext);
			}
			fillTags(span);
		}

		return span;
	}

	private Span createXraySpan(XRaySpanContext xrayParentContext) {
		TraceHeader.SampleDecision sampleDecision = xrayParentContext.getSampleDecision();
		if (sampleDecision == null || TraceHeader.SampleDecision.REQUESTED.equals(sampleDecision)
				|| TraceHeader.SampleDecision.UNKNOWN.equals(sampleDecision)) {
			sampleDecision = this.fromSamplingStrategy();
		}

		Segment segment;
		if (TraceHeader.SampleDecision.SAMPLED == sampleDecision) {
			segment = recorder.beginSegment(name, xrayParentContext.getTraceId(), xrayParentContext.getId());
		} else {
			segment = recorder.beginDummySegment(xrayParentContext.getTraceId());
		}
		return new XRaySpan.XRaySegmentSpan(recorder, segment);
	}

	private TraceHeader.SampleDecision fromSamplingStrategy() {
		if (recorder.getSamplingStrategy()
				.shouldTrace(new SamplingRequest(getServiceName(), getHost(), getPath(), getMethod(), getServiceType()))
				.isSampled()) {
			return TraceHeader.SampleDecision.SAMPLED;
		} else {
			return TraceHeader.SampleDecision.NOT_SAMPLED;
		}
	}

	private String getServiceName() {
		String serviceName = null;
		if (stringTags != null) {
			serviceName = stringTags.get(Tags.PEER_SERVICE.getKey());
		}
		if (serviceName == null) {
			serviceName = name;
		}
		return serviceName;
	}

	private String getPath() {
		if (stringTags != null) {
			return stringTags.get(Tags.HTTP_URL.getKey());
		} else {
			return null;
		}
	}

	private String getHost() {
		if (stringTags != null) {
			return stringTags.get(Tags.PEER_HOSTNAME.getKey());
		} else {
			return null;
		}
	}

	private String getServiceType() {
		if (stringTags != null) {
			return stringTags.get(Tags.SPAN_KIND.getKey());
		} else {
			return null;
		}
	}

	private String getMethod() {
		if (stringTags != null) {
			return stringTags.get(Tags.HTTP_METHOD.getKey());
		} else {
			return "GET";
		}
	}

	private void fillTags(Span span) {
		if (stringTags != null) {
			for (Map.Entry<String, String> entry : stringTags.entrySet()) {
				span.setTag(entry.getKey(), entry.getValue());
			}
		}
		if (booleanTags != null) {
			for (Map.Entry<String, Boolean> entry : booleanTags.entrySet()) {
				span.setTag(entry.getKey(), entry.getValue());
			}
		}
		if (numberTags != null) {
			for (Map.Entry<String, Number> entry : numberTags.entrySet()) {
				span.setTag(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public Span start() {
		return this.startManual();
	}
}
