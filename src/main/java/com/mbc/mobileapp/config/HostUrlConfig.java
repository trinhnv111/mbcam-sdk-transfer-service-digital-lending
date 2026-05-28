package com.mbc.mobileapp.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

@Configuration
@PropertySources({ @PropertySource("classpath:application.properties") })
public class HostUrlConfig {

	@Autowired
	private Environment env; // Contains Properties Load by @PropertySources

	public static String IL_REST_URL;
	public static String GET_CUSTOMER_T24_REST_URL;
	public static String IL_T24_VERSION_REST_URL;
	public static String CARD_GW_IL_REST_URL;

	@PostConstruct
    public void init() {
		IL_REST_URL = env.getProperty("il.rest.url");
		GET_CUSTOMER_T24_REST_URL = env.getProperty("get.customer.t24.rest.url");		
		IL_T24_VERSION_REST_URL = env.getProperty("il.t24.version.rest.url");
		CARD_GW_IL_REST_URL = env.getProperty("card.gw.il.rest.url");
    }
}
