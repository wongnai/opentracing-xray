package com.wongnai.tracing.xray;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.wongnai.common.LocalStack;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.Span;
import io.opentracing.SpanContext;

/**
 * An implementation of {@link ActiveSpanSource} for AWS X-Ray.
 *
 * @author Suparit Krityakien
 */
public class XRayActiveSpanSource implements ActiveSpanSource {
	private final AWSXRayRecorder recorder;
	private LocalStack<XRayActiveSpan> localStack = new LocalStack<>();

	/**
	 * Constructs an instance.
	 *
	 * @param recorder
	 *            recorder
	 */
	public XRayActiveSpanSource(AWSXRayRecorder recorder) {
		this.recorder = recorder;
	}

	/**
	 * Gets recorder.
	 *
	 * @return recorder
	 */
	public AWSXRayRecorder getRecorder() {
		return recorder;
	}

	@Override
	public ActiveSpan activeSpan() {
		if (!localStack.isEmpty()) {
			XRayActiveSpan as = localStack.get();

			if (as != null && as.isActive()) {
				return as;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ActiveSpan makeActive(Span span) {
		XRayActiveSpan as = new XRayActiveSpan(span, (s) -> {
			localStack.push(s);
			recorder.setTraceEntity(getTraceEntity(s));
		}, (s) -> {
			localStack.pop();
			recorder.setTraceEntity(localStack.isEmpty() ? null : getTraceEntity(localStack.get()));
		});

		localStack.push(as);

		return as;
	}

	private Entity getTraceEntity(XRayActiveSpan xRayActiveSpan) {
		Span span = xRayActiveSpan.getSpan();

		SpanContext c = span.context();
		if (c instanceof XRaySpanContext) {
			return ((XRaySpanContext) c).getEntity();
		} else {
			return null;
		}
	}
}
