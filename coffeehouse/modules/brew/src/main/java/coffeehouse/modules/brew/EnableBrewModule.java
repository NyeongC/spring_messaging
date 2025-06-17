package coffeehouse.modules.brew;

import coffeehouse.modules.brew.domain.service.OrderSheetSubmission;
import coffeehouse.modules.brew.domain.service.OrderSheetSubmission.OrderSheetForm;
import coffeehouse.modules.brew.domain.OrderId;
import coffeehouse.modules.order.domain.message.BrewRequestCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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

        @Bean
        public IntegrationFlow requestBrewIntegration(OrderSheetSubmission orderSheetSubmission, MessageChannel barCounterChannel) {

            return IntegrationFlow.from(barCounterChannel)
                    .handle(message -> {
                        var command = (BrewRequestCommand) message.getPayload();
                        var brewOrderId = new OrderId(command.orderId().value());
                        orderSheetSubmission.submit(new OrderSheetForm(brewOrderId));

                    }).get();
        }
    }
}
