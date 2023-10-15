package com.atd.microservices.core.ediaggregator.webclients;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.ediaggregator.exception.EDIAggregatorException;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@Component
public class EDIConfigClient {

	@Autowired
	private WebClient webClient;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${ediaggregator.getEdiConfigSeqnceUrl}")
	private String getEdiConfigSeqnceUrl;
	
	@Value("${ediaggregator.getEdiConfigByCustomerUrl}")
	private String getEdiConfigByCustomerUrl;

	public Mono<Integer> getSequenceNumber(String receiverCode) {

		return webClient.post()
				.uri(getEdiConfigSeqnceUrl, receiverCode)
				.header("XATOM-CLIENTID", applicationName).retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono
								.error(new EDIAggregatorException("EDIReader returned error attempting to call EdiConfig")))
				.toEntity(new ParameterizedTypeReference<Map<String, Integer>>() {})
				.filter(res -> res.getBody() != null)
				.map(res -> res.getBody().get("sequenceNumber"))
				.onErrorResume(e -> Mono.error(new EDIAggregatorException("Error while invoking EDIConfig API", e)));
	}
	
	public Mono<JsonNode> getEdiConfigByCustomer(String customerName) {

		return webClient.get()
				.uri(getEdiConfigByCustomerUrl, customerName)
				.header("XATOM-CLIENTID", applicationName).retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono
								.error(new EDIAggregatorException("EDIReader returned error attempting to call EdiConfig")))
				.bodyToMono(JsonNode.class)
				.onErrorResume(e -> Mono.error(new EDIAggregatorException("Error while invoking EDIConfig API", e)));
	}
}