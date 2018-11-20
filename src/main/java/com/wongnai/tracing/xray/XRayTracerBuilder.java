package com.wongnai.tracing.xray;

import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EC2Plugin;
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy;
import com.amazonaws.xray.strategy.sampling.AllSamplingStrategy;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;
import com.wongnai.common.ResourceUtils;
import com.wongnai.common.StringUtils;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class XRayTracerBuilder {
	private static final String DAEMON_SYSTEM_PROP_NAME = "com.amazonaws.xray.emitters.daemonAddress";
	/**
	 * Sets value indicating if all trace should be sent to deamon or not.
	 *
	 * @param all
	 *            value indicating if all trace should be sent to deamon or not
	 */
	private boolean all;
	/**
	 * Sets rules file.
	 *
	 * @param rulesFile
	 *            rules file e.g. com/wongnai/tracing/xray/default-rules.json
	 */
	private String rulesFile;
	/**
	 * Sets daemon address.
	 *
	 * @param daemonAddress
	 *            daemon address e.g. 127.0.0.1:2000
	 */
	private String daemonAddress;
	/**
	 * Sets context missing strategy.
	 *
	 * 3 choices for now - ignored - warn - error
	 *
	 * @param contextMissingStrategy
	 *            context missing strategy
	 */
	private String contextMissingStrategy;

	private AWSXRayRecorder awsXRayRecorder() {
		if (StringUtils.isBlank(System.getProperty(DAEMON_SYSTEM_PROP_NAME)) && !StringUtils.isBlank(daemonAddress)) {
			log.info("Using X-Ray Daemon at {}.", daemonAddress);
			System.setProperty(DAEMON_SYSTEM_PROP_NAME, daemonAddress);
		}
		AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard();
		if (EC2MetadataUtils.getInstanceId() != null) {
			log.info("Adding EC2Plugin.");
			builder.withPlugin(new EC2Plugin());
		}
		fillContextMissingStrategy(builder);
		if (all) {
			log.info("Using all AllSamplingStrategy.");
			builder.withSamplingStrategy(new AllSamplingStrategy());
		} else {
			log.info("Loading sampling strategy from {}.", rulesFile);
			builder.withSamplingStrategy(new LocalizedSamplingStrategy(ResourceUtils.getURL(rulesFile)));
		}

		AWSXRayRecorder recorder = builder.build();
		registerGlobalRecorder(recorder);

		return recorder;
	}

	private void fillContextMissingStrategy(AWSXRayRecorderBuilder builder) {
		if ("ignored".equals(contextMissingStrategy)) {
			builder.withContextMissingStrategy((s, aClass) -> {
			});
		} else if ("warn".equals(contextMissingStrategy)) {
			builder.withContextMissingStrategy(new LogErrorContextMissingStrategy());
		}
	}

	private void registerGlobalRecorder(AWSXRayRecorder recorder) {
		AWSXRay.setGlobalRecorder(recorder);
	}

	/**
	 * Build tracer.
	 *
	 * @return object
	 */
	public Tracer build() {
		XRayTracer tracer = new XRayTracer(awsXRayRecorder());

		GlobalTracer.register(tracer);

		return tracer;
	}
}
