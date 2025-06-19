package coffeehouse.modules.brew;

import coffeehouse.modules.brew.domain.service.OrderSheetSubmission;
import coffeehouse.modules.brew.domain.service.OrderSheetSubmission.OrderSheetForm;
import coffeehouse.modules.brew.domain.OrderId;
import coffeehouse.modules.order.domain.message.BrewRequestCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.Observable;

/**
 * @author springrunner.kr@gmail.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(EnableBrewModule.BrewModuleConfiguration.class)
public @interface EnableBrewModule {

    @Configuration
    @ComponentScan
    class BrewModuleConfiguration {

        /* 기존 메세지 핸들러
        @Bean
        MessageHandler messageHandler(OrderSheetSubmission orderSheetSubmission, MessageChannel barCounterChannel) {
            var messageHandler = new MessageHandler() {
                @Override
                public void handleMessage(Message<?> message) throws MessagingException {
                    var command = (BrewRequestCommand) message.getPayload();
                    var brewOrderId = new OrderId(command.orderId().value());

                    orderSheetSubmission.submit(new OrderSheetForm(brewOrderId));

                }
            };

            var observer = (Observable) barCounterChannel;
            observer.addObserver((o, arg) -> {
                var message = (Message<?>) arg;
                messageHandler.handleMessage(message);
            });

            return messageHandler;
        } */
        /*
        기존의 ObservableChannel 기반 메시지 수신 로직을 Spring Integration의 **DirectChannel**로 추상화하였고,
        Java DSL을 활용한 IntegrationFlow 구성으로 메시지를 받아 핸들러 메서드에서 처리할 수 있게 되었다.
        이로써 메시지 흐름이 명확하게 구성되고 재사용성도 높아졌다.
        */
        @Bean
        public IntegrationFlow requestBrewIntegration(OrderSheetSubmission orderSheetSubmission, MessageChannel brewRequestChannel) {

            return IntegrationFlow.from(brewRequestChannel)
                    .handle(BrewRequestCommand.class, (payload, message) -> {
                        var command = payload;
                        var brewOrderId = new OrderId(command.orderId().value());
                        orderSheetSubmission.submit(new OrderSheetForm(brewOrderId));
                        return null;
                    }).get();
        }

        @Bean
        MessageChannel brewCompletedNotifyOrderChannel() {
            return new DirectChannel();
        }

        @Bean
        MessageChannel brewCompletedNotifyUserChannel() {
            return new DirectChannel();
        }

        // 이 채널에서 온 메세지를 http 어뎁터와 연결해주면 됨
        @Bean
        public IntegrationFlow notifyOrderIntegration(MessageChannel brewCompletedNotifyOrderChannel, Environment environment) {
            var uri = environment.getRequiredProperty("coffeehouse.brew.notify-brew-complete-uri", URI.class);
            return IntegrationFlow.from(brewCompletedNotifyOrderChannel)
                    .handle(
                            Http.outboundChannelAdapter(uri)
                                    .httpMethod(HttpMethod.POST)
                    ).get();
        }

        @Bean
        public IntegrationFlow notifyUserIntegration(MessageChannel brewCompletedNotifyUserChannel, Environment environment) {
            var uri = environment.getRequiredProperty("coffeehouse.user.notify-brew-complete-uri", URI.class);
            return IntegrationFlow.from(brewCompletedNotifyUserChannel)
                    .handle(
                            Http.outboundChannelAdapter(uri)
                                    .httpMethod(HttpMethod.POST)
                    )
                    .get();
        }

    }
}
