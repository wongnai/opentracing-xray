package com.wongnai.tracing.xray;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.EntityImpl;
import com.amazonaws.xray.entities.Namespace;
import com.amazonaws.xray.entities.Segment;
import com.wongnai.common.ExceptionUtils;
import com.wongnai.tracing.TracingUtils;

import io.opentracing.tag.Tags;

/**
 * All taggers.
 *
 * @author Suparit Krityakien
 */
public final class Taggers {
	private static final Field NAME_FIELD = getFieldName();
	private static final Pattern HTTP_METHOD_PATTERN = Pattern.compile("(GET|POST|get|post|Get|Post)");
	private static final Tagger HTTP_STATUS_TAGGER = (span, entity, key, value) -> {
		span.putHttpResponse("status", value);
		int responseCode = (int) value;
		if (responseCode >= 400) {
			if (responseCode == 429) {
				entity.setError(true);
				entity.setThrottle(true);
			} else if (responseCode >= 500) {
				entity.setFault(true);
			} else {
				entity.setError(true);
			}
		}
	};
	private static final Map<String, Tagger> TAGGERS = createAppliers();

	private Taggers() {
	}

	private static Field getFieldName() {
		try {
			Field field = EntityImpl.class.getDeclaredField("name");
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			throw ExceptionUtils.wrap(e);
		}
	}

	private static Map<String, Tagger> createAppliers() {
		Map<String, Tagger> taggers = new HashMap<>();

		taggers.put(Tags.HTTP_URL.getKey(), (span, entity, key, value) -> span.putHttpRequest("url", value));
		taggers.put(Tags.HTTP_METHOD.getKey(), (span, entity, key, value) -> span.putHttpRequest("method", value));
		taggers.put(TracingUtils.HTTP_USER_AGENT_TAG.getKey(),
				(span, entity, key, value) -> span.putHttpRequest("user_agent", value));
		taggers.put(TracingUtils.HTTP_CLIENT_IP_TAG.getKey(),
				(span, entity, key, value) -> span.putHttpRequest("client_ip", value));
		taggers.put(TracingUtils.HTTP_X_FORWARDED_FOR_TAG.getKey(),
				(span, entity, key, value) -> span.putHttpRequest("x_forwarded_for", value));
		taggers.put(Tags.HTTP_STATUS.getKey(), HTTP_STATUS_TAGGER);
		taggers.put(TracingUtils.HTTP_CONTENT_LENGTH_TAG.getKey(),
				(span, entity, key, value) -> span.putHttpResponse("content_length", value));
		taggers.put(TracingUtils.USER_TAG.getKey(),
				(span, entity, key, value) -> ((Segment) entity).setUser(String.valueOf(value)));
		taggers.put(Tags.SPAN_KIND.getKey(), (span, entity, key, value) -> {
			if (Tags.SPAN_KIND_CLIENT.equals(value)) {
				entity.setNamespace(String.valueOf(Namespace.REMOTE.toString()));
			}
			entity.putMetadata(Tags.SPAN_KIND.getKey(), value);
		});
		taggers.put(Tags.ERROR.getKey(), (span, entity, key, value) -> entity.setError((Boolean) value));
		taggers.put(Tags.PEER_HOSTNAME.getKey(), (span, entity, key, value) -> {
			tryChangeName(entity, String.valueOf(value));
			entity.putMetadata(Tags.PEER_HOSTNAME.getKey(), value);
		});

		return taggers;
	}

	private static void tryChangeName(Entity entity, String name) {
		try {
			if (HTTP_METHOD_PATTERN.matcher(entity.getName()).matches()) {
				NAME_FIELD.set(entity, name);
			}
		} catch (IllegalAccessException e) {
			throw ExceptionUtils.wrap(e);
		}
	}

	/**
	 * Applys tag.
	 *
	 * @param span
	 *            span
	 * @param entity
	 *            entity
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return {@code true} if tag is applied
	 */
	public static boolean applyTag(XRayBaseSpan span, Entity entity, String key, Object value) {
		Tagger tagger = TAGGERS.get(key);

		if (tagger != null) {
			tagger.tag(span, entity, key, value);

			return true;
		} else {
			return false;
		}
	}

	private interface Tagger {
		void tag(XRayBaseSpan span, Entity entity, String key, Object value);
	}
}
