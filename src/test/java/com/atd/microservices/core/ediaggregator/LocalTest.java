package com.atd.microservices.core.ediaggregator;

import java.util.Arrays;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atd.microservices.core.ediaggregator.domain.EDIDocGroupedResult;
import com.atd.microservices.core.ediaggregator.service.EDIAggregatorConsumerService;
import com.atd.microservices.core.ediaggregator.service.EDIDocAggregatorService;
import com.atd.microservices.core.ediaggregator.webclients.EDIConfigClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
		"kafka.bootstrap.server.url=null",
		"kafka.security.protocol=SSL",
		"kafka.topic.inbound=DEDIAGGREGATION",
		"ssl.truststore.password=5orfUb8r9wgw1ClNi1lw",
		"ssl.truststore.location=C:\\atd-projects\\kafka.broker.truststore.jks",
		"ediaggregator.updateEDIDocProcessedFlagUrl=http://localhost:9309/edidoc/update/processed/{processedFlag}",
		"ediaggregator.getEDIDocsByIdUrl=http://localhost:9309/edidoc/id",
		"kafka.analytic.topic=DAPIGATEWAYANALYTICS",
		"ediaggregator.fusePath=C://atd-projects/groupedfiles/",
		"ediaggregator.getEdiConfigSeqnceUrl=https://r-qa-edi.gcp.atd-us.com/ediconfig/sequencenum/{receiverCode}",
		"ediaggregator.getEdiConfigByCustomerUrl=https://r-qa-edi.gcp.atd-us.com/ediconfig/partner/{partner}"})
public class LocalTest {
	
	@MockBean
	private EDIAggregatorConsumerService ediAggregatorConsumerService;
	
	@Autowired
	private EDIConfigClient configClient;
	
	@Autowired
	private EDIDocAggregatorService aggregatorService;
	
	//@Test
	public void test() throws Exception {
		
		/**
		JsonMultipleToEdiConfiguration multiEdiConfig = new JsonMultipleToEdiConfiguration();
		List<Integer> seqnceNumbers = Flux.merge(configClient.getSequenceNumber("12-7047353003"),
				configClient.getSequenceNumber("12-7047353003"), configClient.getSequenceNumber("12-7047353003"))
				.collectList().block();
		if(seqnceNumbers.get(0) != null) {
			multiEdiConfig.setDocumentControlNumber(seqnceNumbers.get(0).toString());
		}
		if(seqnceNumbers.get(1) != null) {
			multiEdiConfig.setGroupControlNumber(seqnceNumbers.get(1).toString());
		}
		if(seqnceNumbers.get(2) != null) { 
			multiEdiConfig.setInterchangeControlNumber(seqnceNumbers.get(2).toString());
		}
		log.info("{}", multiEdiConfig);
		**/
				
		
		EDIDocGroupedResult groupedResult = new EDIDocGroupedResult("monro", "810", "004010",
				Arrays.asList("64df6b551344c033979f263c", "64f887c435df5330ed1f1649", "64fa25e4b34ae603acdcfe08"),
				"TEST_", "ZZ-7047353003T");
		aggregatorService.process(groupedResult);
		
	}
}
