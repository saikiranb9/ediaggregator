package com.atd.microservices.core.ediaggregator.service;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.atd.microservices.core.ediaggregator.domain.EDIDocGroupedResult;
import com.atd.microservices.core.ediaggregator.webclients.EDIConfigClient;
import com.atd.microservices.core.ediaggregator.webclients.EDICoreDataClient;
import com.berryworks.edireader.json.toedi.JsonMultipleToEdi;
import com.berryworks.edireader.json.toedi.JsonMultipleToEdiConfiguration;
import com.berryworks.ediwriter.envelope.EnvelopeSpecification;
import com.berryworks.ediwriter.envelope.EnvelopeSpecificationImpl;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class EDIDocAggregatorService {
	@Autowired
	private EDICoreDataClient coreDataClient;
	@Value("${ediaggregator.fusePath}")
	private String fusePath;
	@Autowired
	private EDIConfigClient configClient;
	@Autowired
	private EDIAggregatorMetrics ediAggregatorMetrics;
	private JsonMultipleToEdi jsonMultipleToEdi = new JsonMultipleToEdi();
	
	public static String REPETITIONSEPARATOR = "repetitionSeparator";
	public static String DELIMITER = "delimiter";
	public static String SUBDELIMITER = "subDelimiter";
	public static String TERMINATOR = "terminator";	
	
	public void process(EDIDocGroupedResult groupedResult) {
		String customer = groupedResult.getCustomer();
		String type = groupedResult.getType();
		String version = groupedResult.getVersion();		
		List<String> mongoIds = groupedResult.getMongoIds();
		String receiverCode = groupedResult.getReceiverCode();
		EnvelopeSpecification envSpecification = new EnvelopeSpecificationImpl();
		boolean isEnvPresent = false;
		try {
			// Fetch EDIJsons from DB
			List<String> ediJsons = coreDataClient.getEDIDocsByIds(mongoIds)
					.filter(doc -> StringUtils.isNotBlank(doc.getEdiJson()))
					.map(doc -> doc.getEdiJson()).collectList()
					.block();
			if(ediJsons != null && ediJsons.size() > 0) {
				/**
				 * Create config object before joining
				 */
				JsonMultipleToEdiConfiguration multiEdiConfig = new JsonMultipleToEdiConfiguration();			
				if(StringUtils.isNotBlank(receiverCode)) {
					List<Integer> seqnceNumbers = Flux.merge(configClient.getSequenceNumber(receiverCode),
							configClient.getSequenceNumber(receiverCode))
							.collectList().block();
					
					multiEdiConfig.setDocumentControlNumber("1001"); // TODO - change this logic to make it dynamic according the GS number
					
					if(seqnceNumbers.get(0) != null) { 
						multiEdiConfig.setInterchangeControlNumber(seqnceNumbers.get(0).toString());
					}
					if(seqnceNumbers.get(1) != null) {
						multiEdiConfig.setGroupControlNumber(seqnceNumbers.get(1).toString());
					}				
				}
				/**
				 * Delimiters configs
				 */
				JsonNode config = configClient.getEdiConfigByCustomer(customer).block();
				if(config != null) {
					if(config.get(REPETITIONSEPARATOR) != null && config.get(REPETITIONSEPARATOR).textValue() != null) {
						envSpecification.setRepetitionSeparator(Integer.parseInt(config.get(REPETITIONSEPARATOR).textValue()));
						isEnvPresent = true;
					} else {
						envSpecification.setRepetitionSeparator(0);
					}
					if(config.get(DELIMITER) != null && config.get(DELIMITER).textValue() != null) {
						envSpecification.setDelimiter(config.get(DELIMITER).textValue().charAt(0));
						isEnvPresent = true;
					}
					if(config.get(SUBDELIMITER) != null && config.get(SUBDELIMITER).textValue() != null) {
						envSpecification.setSubDelimiter(config.get(SUBDELIMITER).textValue().charAt(0));
						isEnvPresent = true;					
					}
					if(config.get(TERMINATOR) != null && config.get(TERMINATOR).textValue() != null) {
						envSpecification.setTerminator(config.get(TERMINATOR).textValue().charAt(0));	
						isEnvPresent = true;
					}
				}
				
				Writer writer = new StringWriter();
				if (isEnvPresent) {
					jsonMultipleToEdi.asEdi(ediJsons, writer, multiEdiConfig, envSpecification);
				} else {
					jsonMultipleToEdi.asEdi(ediJsons, writer, multiEdiConfig);
				}
		        String ediString = writer.toString();
		        if(log.isDebugEnabled()) {
		        	log.debug("Joined EDI Document for {}-{}-{}: {}", customer, type, version, ediString);
		        }
		        
		        String fileName = String.format(("%s/%s/%s/%s_%s.edi"), fusePath, customer,
						type, groupedResult.getFileName(), Instant.now().toEpochMilli());
		        if(log.isDebugEnabled()) {
		        	log.debug("File Path: {}", fileName);
		        }
		        Files.write(Paths.get(fileName), ediString.getBytes(StandardCharsets.UTF_8));
		        try {
		        	ediAggregatorMetrics.increaseTotalGroupedMsgCreatedCount();
				} catch (Exception e) {}
			}
		} catch (Exception e) {
			log.error("Error while grouping the edi docs", e);
			try {
				log.info("Trying to reset the {} flag to False", "PROCESSED");
				coreDataClient.updateEDIDocProcessedFlag(mongoIds, Boolean.FALSE);
			} catch (Exception e2) {
				log.error("Could not reset the EDIDoc Processed flag");
			}
			
		}
	}

}
