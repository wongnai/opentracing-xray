package com.wongnai.tracing;

import java.util.HashMap;
import java.util.Map;

import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.BooleanTag;
import io.opentracing.tag.IntTag;
import io.opentracing.tag.StringTag;
import io.opentracing.util.GlobalTracer;

/**
 * Utilities for working with tracing.
 *
 * @author Suparit Krityakien
 */
public final class TracingUtils {
	public static final StringTag HTTP_USER_AGENT_TAG = new StringTag("http.user_agent");
	public static final StringTag HTTP_CLIENT_IP_TAG = new StringTag("http.client_ip");
	public static final BooleanTag HTTP_X_FORWARDED_FOR_TAG = new BooleanTag("http.x_forwarded_for");
	public static final IntTag HTTP_CONTENT_LENGTH_TAG = new IntTag("http.content_length");
	public static final StringTag USER_TAG = new StringTag("user");

	private TracingUtils() {
	}

	/**
	 * Adds exception.
	 *
	 * @param t
	 *            exception
	 */
	public static void addException(Throwable t) {
		execute((activeSpan) -> addException(activeSpan, t));
	}

	/**
	 * Adds exception.
	 *
	 * @param span
	 *            span
	 * @param t
	 *            exception
	 */
	public static void addException(Span span, Throwable t) {
		if (t != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("error", t);
			span.log(map);
		}
	}

	/**
	 * Adds exception.
	 *
	 * @param activeSpan
	 *            active span
	 * @param t
	 *            exception
	 */
	public static void addException(ActiveSpan activeSpan, Throwable t) {
		if (t != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("error", t);
			activeSpan.log(map);
		}
	}

	/**
	 * Executes in active span.
	 *
	 * Do nothing if there is no active span.
	 *
	 * @param inActiveSpan
	 *            in active span
	 */
	public static void execute(InActiveSpan inActiveSpan) {
		execute(GlobalTracer.get(), inActiveSpan);
	}

	/**
	 * Executes in active span.
	 *
	 * Do nothing if there is no active span.
	 *
	 * @param tracer
	 *            tracer
	 * @param inActiveSpan
	 *            in active span
	 */
	public static void execute(Tracer tracer, InActiveSpan inActiveSpan) {
		if (tracer != null) {
			ActiveSpan activeSpan = tracer.activeSpan();
			if (activeSpan != null) {
				inActiveSpan.execute(activeSpan);
			}
		}
	}

	/**
	 * Sets user id.
	 *
	 * @param userId
	 *            user id
	 */
	public static void setUserId(String userId) {
		execute((activeSpan) -> USER_TAG.set(activeSpan, userId));
	}

	/**
	 * Sets user id.
	 *
	 * @param tracer
	 *            tracer
	 * @param userId
	 *            user id
	 */
	public static void setUserId(Tracer tracer, String userId) {
		execute(tracer, (activeSpan) -> USER_TAG.set(activeSpan, userId));
	}

	public interface InActiveSpan {
		/**
		 * Executes in active span.
		 *
		 * @param activeSpan
		 *            active span
		 */
		void execute(ActiveSpan activeSpan);
	}
}
