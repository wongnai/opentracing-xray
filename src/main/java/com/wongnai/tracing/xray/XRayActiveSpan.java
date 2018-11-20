package com.wongnai.tracing.xray;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.SpanContext;

/**
 * An implementation of {@link ActiveSpan} for AWS X-Ray.
 */
public class XRayActiveSpan implements ActiveSpan {
	private final Span span;
	private int count = 1;
	private XRayActiveSpan.C c;
	private Consumer<XRayActiveSpan> activateListener;
	private Consumer<XRayActiveSpan> deactivateListener;

	/**
	 * Constructs an instance.
	 *
	 * @param span
	 *            span
	 * @param activateListener
	 *            activate listener
	 * @param deactivateListener
	 *            deactivate listener
	 */
	public XRayActiveSpan(Span span, Consumer<XRayActiveSpan> activateListener, Consumer<XRayActiveSpan> deactivateListener) {
		this.span = span;
		this.activateListener = activateListener;
		this.deactivateListener = deactivateListener;
	}

	@Override
	public void deactivate() {
		count--;
		if (count == 0) {
			span.finish();
		}
		deactivateListener.accept(this);
	}

	@Override
	public void close() {
		deactivate();
	}

	@Override
	public Continuation capture() {
		if (c == null) {
			c = new C();
		}
		count++;

		return c;
	}

	@Override
	public SpanContext context() {
		return span.context();
	}

	@Override
	public ActiveSpan setTag(String key, String value) {
		span.setTag(key, value);

		return this;
	}

	@Override
	public ActiveSpan setTag(String key, boolean value) {
		span.setTag(key, value);

		return this;
	}

	@Override
	public ActiveSpan setTag(String key, Number value) {
		span.setTag(key, value);

		return this;
	}

	@Override
	public ActiveSpan log(Map<String, ?> fields) {
		span.log(fields);

		return this;
	}

	@Override
	public ActiveSpan log(long timestampMicroseconds, Map<String, ?> fields) {
		span.log(timestampMicroseconds, fields);

		return this;
	}

	@Override
	public ActiveSpan log(String event) {
		span.log(event);

		return this;
	}

	@Override
	public ActiveSpan log(long timestampMicroseconds, String event) {
		span.log(timestampMicroseconds, event);

		return this;
	}

	@Override
	public ActiveSpan setBaggageItem(String key, String value) {
		span.setBaggageItem(key, value);

		return this;
	}

	@Override
	public String getBaggageItem(String key) {
		return span.getBaggageItem(key);
	}

	@Override
	public ActiveSpan setOperationName(String operationName) {
		span.setOperationName(operationName);

		return this;
	}

	@Override
	public ActiveSpan log(String eventName, Object payload) {
		span.log(createLogs(eventName, payload));

		return this;
	}

	private HashMap<String, Object> createLogs(String eventName, Object payload) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("event", eventName);
		map.put("payload", payload);
		return map;
	}

	@Override
	public ActiveSpan log(long timestampMicroseconds, String eventName, Object payload) {
		span.log(timestampMicroseconds, createLogs(eventName, payload));

		return this;
	}

	/**
	 * Checks if this active span is really still active.
	 *
	 * @return {@code true} if this active span is really still active
	 */
	public boolean isActive() {
		return count > 0;
	}

	/**
	 * Gets span.
	 *
	 * @return span
	 */
	public Span getSpan() {
		return span;
	}

	private class C implements Continuation {
		@Override
		public ActiveSpan activate() {
			activateListener.accept(XRayActiveSpan.this);

			return XRayActiveSpan.this;
		}
	}
}
