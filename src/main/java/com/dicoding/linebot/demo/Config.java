package com.dicoding.linebot.demo;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("clashpath:application.properties")
public class Config {

	@Autowired
	private Environment mEnv;

	@Bean(name = "com.linecorp.channel_secret")
	public String getChannelSecret() {
		return mEnv.getProperty("com.linecorp.channel_secret");
	}

	@Bean(name = "com.linecorp.channel_access_token")
	public String getAccessToken() {
		return mEnv.getProperty("com.linecorp.channel_access_token");
	}

	@Bean(name = "lineMessagingClient")
	public LineMessagingClient getMessageClient() {
		return LineMessagingClient.builder(getAccessToken())
				.apiEndPoint("https://api.line.me/")
				.connectTimeout(10_000)
				.readTimeout(10_000)
				.writeTimeout(10_000)
				.build();
	}
	
	@Bean(name="lineSignatureValidator")
	public LineSignatureValidator getSignatureValidator() {
		return new LineSignatureValidator(getChannelSecret().getBytes());
	}

}
