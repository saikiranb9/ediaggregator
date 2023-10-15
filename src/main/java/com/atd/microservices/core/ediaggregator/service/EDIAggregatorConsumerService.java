package com.atd.microservices.core.ediaggregator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.atd.microservices.core.ediaggregator.configuration.KafkaConfigConstants;
import com.atd.microservices.core.ediaggregator.domain.EDIDocGroupedResult;
import com.atd.utilities.kafkalogger.constants.AnalyticsContants;
import com.atd.utilities.kafkalogger.operation.KafkaAnalyticsLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EDIAggregatorConsumerService {	
	
	@Autowired
	private KafkaAnalyticsLogger serviceKafkaLogger;
	
	@Autowired
	private KafkaConfigConstants kafkaConfigConstants;
	
	@Autowired
	private TracerUtil tracerUtil;
	
	@Value("${spring.application.name}")
	private String appName;	
	
	@Autowired
	private EDIDocAggregatorService aggregatorService;
	
	@Autowired
	private EDIAggregatorMetrics ediAggregatorMetrics;
	
	@Autowired
	private ThreadUtil threadUtil;

	@KafkaListener(topics = "${kafka.topic.inbound}", groupId = "group_ediaggregator", containerFactory = "kafkaListenerContainerFactory")
	public void analyticsMessageListener(@Payload EDIDocGroupedResult payload) {		
		if(log.isDebugEnabled()) {
			log.debug("Recieved message: " + payload);
		}
		try {
			ediAggregatorMetrics.increaseTotalIncomingGroupedMsgCount();
			serviceKafkaLogger.logObject(AnalyticsContants.MessageSourceType.MESSAGE_SOURCE_TYPE_CONSUMER,
					kafkaConfigConstants.KAFKA_TOPIC_INBOUND, appName, payload, tracerUtil.getTraceId(), "2xx");
		} catch (Exception e) {
			log.error("Error logging kafka messages to Analytics topic");
		}
		TraceableExecutorService executorService = threadUtil.getFixedPoolExecutorService();
		try {
			executorService.submit(() -> aggregatorService.process(payload));
		} catch (Exception e) {
			log.error("Failed in processing kakfa message", e);
		} finally {
			executorService.shutdown();
		}		
	}
}
