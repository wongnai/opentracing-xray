package com.wongnai.tracing.xray;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;

import io.opentracing.Span;

/**
 * Abstract span.
 *
 * @param <E>
 *            type of entity
 */
public abstract class XRaySpan<E extends Entity> extends XRayBaseSpan<E, Span> implements Span {
	private boolean ended;

	/**
	 * Constructs an instance.
	 *
	 * @param recorder
	 *            recorder
	 * @param entity
	 *            entity
	 */
	protected XRaySpan(AWSXRayRecorder recorder, E entity) {
		super(recorder, entity);
	}

	/**
	 * Creates new span.
	 *
	 * @param recorder
	 *            recorder
	 * @param entity
	 *            entity
	 * @return span
	 */
	public static XRaySpan create(AWSXRayRecorder recorder, Entity entity) {
		if (entity instanceof Segment) {
			return new XRaySegmentSpan(recorder, (Segment) entity);
		} else if (entity instanceof Subsegment) {
			return new XRaySubsegmentSpan(recorder, (Subsegment) entity);
		} else {
			return null;
		}
	}

	@Override
	public void finish(long l) {
		finish();
	}

	@Override
	public void finish() {
		if (!ended) {
			getRecorder().setTraceEntity(getEntity());
			finishInternally();
			ended = true;
		}
	}

	/**
	 * Finishes.
	 */
	protected abstract void finishInternally();

	/**
	 * Span for AWS X-Ray segment.
	 *
	 * @author Suparit Krityakien
	 */
	public static class XRaySegmentSpan extends XRaySpan<Segment> {
		/**
		 * Constructs an instance.
		 *
		 * @param recorder
		 *            recorder
		 * @param segment
		 *            segment
		 */
		public XRaySegmentSpan(AWSXRayRecorder recorder, Segment segment) {
			super(recorder, segment);
		}

		/**
		 * Creates new span.
		 *
		 * @param recorder
		 *            recorder
		 * @param segment
		 *            segment
		 * @return new span
		 */
		public static XRaySegmentSpan create(AWSXRayRecorder recorder, Segment segment) {
			return new XRaySegmentSpan(recorder, segment);
		}

		@Override
		protected XRaySpanContext createSpanContext() {
			return XRaySpanContext.create(getEntity());
		}

		@Override
		protected void finishInternally() {
			getRecorder().endSegment();
		}
	}

	/**
	 * Span for AWS X-Ray subsegment.
	 *
	 * @author Suparit Krityakien
	 */
	public static class XRaySubsegmentSpan extends XRaySpan<Subsegment> {
		/**
		 * Constructs an instance.
		 *
		 * @param recorder
		 *            recorder
		 * @param subsegment
		 *            subsegment
		 */
		public XRaySubsegmentSpan(AWSXRayRecorder recorder, Subsegment subsegment) {
			super(recorder, subsegment);
		}

		/**
		 * Creates new span.
		 *
		 * @param recorder
		 *            recorder
		 * @param subsegment
		 *            subsegment
		 * @return new span
		 */
		public static XRaySubsegmentSpan create(AWSXRayRecorder recorder, Subsegment subsegment) {
			return new XRaySubsegmentSpan(recorder, subsegment);
		}

		@Override
		protected XRaySpanContext createSpanContext() {
			return XRaySpanContext.create(getEntity());
		}

		@Override
		protected void finishInternally() {
			getRecorder().endSubsegment();
		}
	}
}
