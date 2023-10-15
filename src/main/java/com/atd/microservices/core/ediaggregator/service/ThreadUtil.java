package com.atd.microservices.core.ediaggregator.service;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.stereotype.Component;

@Component
public class ThreadUtil {
	
	@Autowired
	private BeanFactory beanFactory;
	
	public TraceableExecutorService getFixedPoolExecutorService() {
		return new TraceableExecutorService(beanFactory,
				Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()), "ediaggregator-fixedPool");
	}

}
