package com.atd.microservices.core.ediaggregator.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.atd.microservices.core.ediaggregator.domain.EDIDocGroupedResult;

@EnableKafka
@Configuration
public class KafkaConfiguration {
	
	@Value("${spring.application.name:creditcardtransactions}")
	private String applicationName;

	@Autowired
	private KafkaConfigConstants kafkaConfigConstants;

	public ConsumerFactory<String, EDIDocGroupedResult> consumerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigConstants.BOOTSTRAP_SERVER_URL);
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_ediaggregator");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");		
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer2.class);
		config.put(ErrorHandlingDeserializer2.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
		config.put(ErrorHandlingDeserializer2.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
		config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.atd.microservices.core.ediaggregator.domain.EDIDocGroupedResult");
		config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
		config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		
		config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, kafkaConfigConstants.SECURITY_PROTOCOL);
		if (kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION != null
				&& !StringUtils.isEmpty(kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION)) {
			config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, kafkaConfigConstants.SSL_TRUSTSTORE_LOCATION);
		}
		config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaConfigConstants.SSL_TRUSTSTORE_PASSWORD);
		config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

		return new DefaultKafkaConsumerFactory<>(config);		
	}	

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, EDIDocGroupedResult> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, EDIDocGroupedResult> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}

}
