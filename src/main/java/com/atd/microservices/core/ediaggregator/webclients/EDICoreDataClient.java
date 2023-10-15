package com.atd.microservices.core.ediaggregator.webclients;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.atd.microservices.core.ediaggregator.domain.EDIDoc;
import com.atd.microservices.core.ediaggregator.exception.EDIAggregatorException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class EDICoreDataClient {
	
	@Autowired
	private WebClient webClient;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${ediaggregator.updateEDIDocProcessedFlagUrl}")
	private String updateEDIDocProcessedFlagUrl;
	
	@Value("${ediaggregator.getEDIDocsByIdUrl}")
	private String getEDIDocsByIdUrl;
	
	
	public Mono<Long> updateEDIDocProcessedFlag(List<String> mongoIds, Boolean processedFlag) {
		
		return webClient.put()
				.uri(updateEDIDocProcessedFlagUrl, processedFlag)
				.header("XATOM-CLIENTID", applicationName)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(mongoIds))
				.retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono.error(
								new EDIAggregatorException("EDICoreData returned error attempting to call update EDIDocs")))
				.bodyToMono(Long.class)
				.onErrorResume(e -> Mono.error(new EDIAggregatorException(
						"Error while invoking update EDIDocs API", e)));		
	}
	
	public Flux<EDIDoc> getEDIDocsByIds(List<String> mongoIds) {
		
		return webClient.post()
				.uri(getEDIDocsByIdUrl)
				.header("XATOM-CLIENTID", applicationName)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(mongoIds))
				.retrieve()
				.onStatus(HttpStatus::isError,
						exceptionFunction -> Mono.error(
								new EDIAggregatorException("EDICoreData returned error attempting to call update EDIDocs")))
				.bodyToFlux(EDIDoc.class)
				.onErrorResume(e -> Flux.error(new EDIAggregatorException(
						"Error while invoking update EDIDocs API", e)));		
	}

}
