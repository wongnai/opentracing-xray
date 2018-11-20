package com.wongnai.tracing.xray;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;

import io.opentracing.BaseSpan;
import io.opentracing.SpanContext;

/**
 * Base class for all xray span.
 *
 * @param <E>
 *            Type of entity
 * @param <S>
 *            Type of span
 */
public abstract class XRayBaseSpan<E extends Entity, S extends BaseSpan<S>> implements BaseSpan<S> {
	private final AWSXRayRecorder recorder;
	private E entity;
	private HashMap<String, Object> httpRequest;
	private HashMap<String, Object> httpResponse;
	private XRaySpanContext spanContext;

	/**
	 * Constructs an instance.
	 *
	 * @param recorder
	 *            recorder
	 * @param entity
	 *            entity
	 */
	public XRayBaseSpan(AWSXRayRecorder recorder, E entity) {
		this.recorder = recorder;
		this.entity = entity;
	}

	/**
	 * Gets recorder.
	 *
	 * @return recorder
	 */
	public AWSXRayRecorder getRecorder() {
		return recorder;
	}

	/**
	 * Puts http request.
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	void putHttpRequest(String key, Object value) {
		if (httpRequest == null) {
			httpRequest = new HashMap<>();
			entity.putHttp("request", httpRequest);
		}
		httpRequest.put(key, value);
	}

	/**
	 * Puts http response.
	 *
	 * @param key
	 *            key
	 * @param value
	 *            value
	 */
	void putHttpResponse(String key, Object value) {
		if (httpResponse == null) {
			httpResponse = new HashMap<>();
			entity.putHttp("response", httpResponse);
		}
		httpResponse.put(key, value);
	}

	/**
	 * Gets entity.
	 *
	 * @return entity
	 */
	public E getEntity() {
		return entity;
	}

	@Override
	public SpanContext context() {
		if (spanContext == null) {
			spanContext = createSpanContext();
		}
		return spanContext;
	}

	/**
	 * Creates span context.
	 *
	 * @return span context
	 */
	protected abstract XRaySpanContext createSpanContext();

	@Override
	public S setTag(String key, String value) {
		setTagInternal(key, value);

		return (S) this;
	}

	private void setTagInternal(String key, Object value) {
		if (!Taggers.applyTag(this, entity, key, value)) {
			entity.putMetadata(key, value);
		}
	}

	@Override
	public S setTag(String key, boolean value) {
		setTagInternal(key, value);

		return (S) this;
	}

	@Override
	public S setTag(String key, Number value) {
		setTagInternal(key, value);

		return (S) this;
	}

	@Override
	public S log(Map<String, ?> fields) {
		for (Map.Entry<String, ?> field : fields.entrySet()) {
			if (field.getValue() instanceof Throwable) {
				entity.addException((Throwable) field.getValue());
			}
		}

		return (S) this;
	}

	@Override
	public S log(long timestampMicroseconds, Map<String, ?> fields) {
		log(fields);

		return (S) this;
	}

	@Override
	public S log(String event) {
		return (S) this;
	}

	@Override
	public S log(long timestampMicroseconds, String event) {
		return (S) this;
	}

	@Override
	public S setBaggageItem(String key, String value) {
		return (S) this;
	}

	@Override
	public String getBaggageItem(String key) {
		return null;
	}

	@Override
	public S setOperationName(String operationName) {
		return (S) this;
	}

	@Override
	public S log(String eventName, Object payload) {
		return (S) this;
	}

	@Override
	public S log(long timestampMicroseconds, String eventName, Object payload) {
		return (S) this;
	}
}
