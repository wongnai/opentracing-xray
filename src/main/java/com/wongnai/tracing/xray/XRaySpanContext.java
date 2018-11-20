package com.wongnai.tracing.xray;

import java.util.Map;

import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;

import io.opentracing.SpanContext;

/**
 * An implementation of {@link SpanContext} for aws xray.
 *
 * @author Suparit Krityakien
 */
public class XRaySpanContext implements SpanContext {
	private final TraceID traceId;
	private final String parentId;
	private final String id;
	private final TraceHeader.SampleDecision sampleDecision;
	private final Entity entity;

	/**
	 * Constructs an instance.
	 */
	public XRaySpanContext() {
		this(new TraceID(), null, null, null, null);
	}

	/**
	 * Constructs an instance.
	 *
	 * @param traceId
	 *            trace id
	 * @param parentId
	 *            parent id
	 * @param id
	 *            id
	 * @param sampleDecision
	 *            sample decision
	 * @param entity
	 *            entity
	 */
	public XRaySpanContext(TraceID traceId, String parentId, String id, TraceHeader.SampleDecision sampleDecision,
			Entity entity) {
		this.traceId = traceId;
		this.parentId = parentId;
		this.id = id;
		this.sampleDecision = sampleDecision;
		this.entity = entity;
	}

	/**
	 * Creates from a segment.
	 *
	 * @param segment
	 *            segment
	 * @return span context
	 */
	public static XRaySpanContext create(Segment segment) {
		return new XRaySpanContext(segment.getTraceId(), segment.getParentId(), segment.getId(), getDesicion(segment.isSampled()),
				segment);
	}

	private static TraceHeader.SampleDecision getDesicion(Boolean sampled) {
		if (sampled != null) {
			return sampled ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED;
		} else {
			return null;
		}
	}

	/**
	 * Creates from subsegment.
	 *
	 * @param subsegment
	 *            subsemgnet
	 * @return span context
	 */
	public static XRaySpanContext create(Subsegment subsegment) {
		Segment parentSegment = subsegment.getParentSegment();

		String parentId = subsegment.getParentId();
		if (parentId == null) {
			parentId = parentSegment.getId();
		}

		return new XRaySpanContext(parentSegment.getTraceId(), parentId, subsegment.getId(),
				getDesicion(subsegment.getParentSegment().isSampled()), subsegment);
	}

	/**
	 * Creates from trace header.
	 *
	 * @param traceHeader
	 *            tarcer hander
	 * @return span context
	 */
	public static XRaySpanContext create(TraceHeader traceHeader) {
		return new XRaySpanContext(traceHeader.getRootTraceId(), null, traceHeader.getParentId(), traceHeader.getSampled(), null);
	}

	/**
	 * Gets entity.
	 *
	 * @return entity
	 */
	public Entity getEntity() {
		return entity;
	}

	@Override
	public Iterable<Map.Entry<String, String>> baggageItems() {
		return null;
	}

	/**
	 * Gets trace id.
	 *
	 * @return tracer id
	 */
	public TraceID getTraceId() {
		return traceId;
	}

	/**
	 * Gets parent id.
	 *
	 * @return parent id
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * Gets id.
	 *
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets sample decision.
	 *
	 * @return sample decision
	 */
	public TraceHeader.SampleDecision getSampleDecision() {
		return sampleDecision;
	}
}
