package com.example.trigger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.JmsMessageDrivenEndpoint;
import org.springframework.integration.xml.xpath.XPathEvaluationType;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.MessageChannel;

import javax.jms.ConnectionFactory;

@Configuration
@EnableIntegration
public class JobLaunchConfiguration {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public MessageChannel inputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setDestinationName("trades");
        return simpleMessageListenerContainer;
    }

    @Bean
    public JmsMessageDrivenEndpoint jmsMessageDrivenEndpoint() {
        ChannelPublishingJmsMessageListener channelPublishingJmsMessageListener = new ChannelPublishingJmsMessageListener();
        channelPublishingJmsMessageListener.setRequestChannel(inputChannel());
        JmsMessageDrivenEndpoint jmsMessageDrivenEndpoint = new JmsMessageDrivenEndpoint(messageListenerContainer(),
                channelPublishingJmsMessageListener);

        return jmsMessageDrivenEndpoint;
    }

    @Bean
    public IntegrationFlow myFlow() {
        return IntegrationFlows.from("outputChannel")
                .transform(Transformers.xpath("/trade/stock", XPathEvaluationType.STRING_RESULT))
                .handle(logger())
                .get();
    }

    @Bean
    LoggingHandler logger() {
        return new LoggingHandler("INFO");
    }
}