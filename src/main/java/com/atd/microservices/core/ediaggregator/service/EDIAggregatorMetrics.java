package com.atd.microservices.core.ediaggregator.service;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class EDIAggregatorMetrics {
	
	public static String METRIC_TOTAL_INCOMING_GROUPED_EDI = "ediaggregator_total_incoming_grouped_edi_docs";
	public static String METRIC_TOTAL_GROUPED_EDI_CREATED = "ediaggregator_total_grouped_edi_docs_created";
	
	MeterRegistry meterRegistry;

	public EDIAggregatorMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}
	
	public void increaseTotalIncomingGroupedMsgCount() {
		this.meterRegistry.counter(METRIC_TOTAL_INCOMING_GROUPED_EDI).increment();
	}
	
	public void increaseTotalGroupedMsgCreatedCount() {
		this.meterRegistry.counter(METRIC_TOTAL_GROUPED_EDI_CREATED).increment();
	}

}
