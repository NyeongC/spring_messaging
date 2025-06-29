package coffeehouse.tests.integration;

import coffeehouse.libraries.message.ObservableChannel;
import coffeehouse.modules.brew.EnableBrewModule;
import coffeehouse.modules.brew.domain.OrderSheetId;
import coffeehouse.modules.brew.domain.entity.OrderSheet;
import coffeehouse.modules.brew.domain.entity.OrderSheetRepository;
import coffeehouse.modules.brew.domain.entity.OrderSheetStatus;
import coffeehouse.modules.order.EnableOrderModule;
import coffeehouse.modules.order.domain.OrderId;
import coffeehouse.modules.order.domain.UserAccountId;
import coffeehouse.modules.order.domain.entity.Order;
import coffeehouse.modules.order.domain.entity.OrderRepository;
import coffeehouse.modules.order.domain.entity.OrderStatus;
import coffeehouse.modules.user.EnableUserModule;
import coffeehouse.modules.user.domain.entity.UserAccount;
import coffeehouse.modules.user.domain.entity.UserAccountRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.client.RestTemplate;

/**
 * @author springrunner.kr@gmail.com
 */
@SpringBootApplication
@EnableOrderModule
@EnableBrewModule
@EnableUserModule
public class CoffeehouseIntegrationTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeehouseIntegrationTestingApplication.class, args);
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        var rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(new Queue("brew"));
        return rabbitAdmin;
    }

    @Bean
    InitializingBean initData(OrderSheetRepository orderSheetRepository, OrderRepository orderRepository, UserAccountRepository userAccountRepository) {
        return () -> {
            var userAccountIdValue = "bb744f5a-2715-488b-aade-ebae5aa8f055";
            userAccountRepository.save(UserAccount.createCustomer(new coffeehouse.modules.user.domain.UserAccountId(userAccountIdValue)));

            var newOrderIdValue = "7438b60b-7c68-4d55-a033-fa933e92832c";

            orderRepository.save(Order.create(new OrderId(newOrderIdValue), new UserAccountId(userAccountIdValue)));


            var acceptedOrderIdValue = "1a176aa8-e834-46e8-b293-0d0208ad1cd8";
            var confirmedOrderSheetIdValue = "e9c17eeb-2bbf-4087-acd3-9675eb6178db";
            orderRepository.save(new Order(new OrderId(acceptedOrderIdValue), new UserAccountId(userAccountIdValue), OrderStatus.ACCEPTED));
            orderSheetRepository.save(new OrderSheet(new OrderSheetId(confirmedOrderSheetIdValue), new coffeehouse.modules.brew.domain.OrderId(acceptedOrderIdValue), OrderSheetStatus.CONFIRMED));
        };
    }

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    MessageChannel barCounterChannel() {
        return new DirectChannel();
    }

    @Bean
    MessageChannel brewRequestChannel() {
        return new DirectChannel();
    }
    
    @Bean
    RestTemplate defaultRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }


//    @Bean
//    public IntegrationFlow amqpOutboundIntegrationChannel(AmqpTemplate amqpTemplate, MessageChannel barCounterChannel) {
//        return IntegrationFlow.from(barCounterChannel)
//                .handle(
//                        Amqp.outboundAdapter(amqpTemplate)
//                                .routingKey("brew")
//                ).get();
//    }
    /*
     * JAVADSL -> 애너테이션 변환
     * flow가 많아지고 중복코드도 많이 발생할듯
     * */

    @Bean
    @ServiceActivator(inputChannel = "barCounterChannel")
    //  @ServiceActivator 이걸 사용하면 메세지 채널에서온 메세지를 amqpOutbound 엔드포인트로 보낼 수 있음
    public AmqpOutboundEndpoint amqpOutboundEndpoint(AmqpTemplate amqpTemplate) {
        var amqpOutboundEndpoint = new AmqpOutboundEndpoint(amqpTemplate);
        amqpOutboundEndpoint.setRoutingKey("brew"); // brew 라는 라우팅키로
        return amqpOutboundEndpoint;
    }

    @Bean
    @Router(inputChannel = "amqpInboundChannel")
    public HeaderValueRouter messageRouter() {
        var router = new HeaderValueRouter("amqp_receivedRoutingKey");
        router.setChannelMapping("brew", "brewRequestChannel");
        return router;
    }

    @Bean
    MessageChannel amqpInboundChannel() {
        return new QueueChannel();
    }

    /*@Bean
    public IntegrationFlow amqpInboundIntegrationChannelFlow(ConnectionFactory connectionFactory, MessageChannel brewRequestChannel) {
        return IntegrationFlow.from(
                Amqp.inboundAdapter(connectionFactory, "brew")
        ).handle(
                message -> {
                    brewRequestChannel.send(message);
                }
        ).get();
    }*/

    @Bean
    SimpleMessageListenerContainer amqpContainer(ConnectionFactory connectionFactory) {
        var amqpContainer = new SimpleMessageListenerContainer(connectionFactory);
        amqpContainer.addQueueNames("brew");
        return amqpContainer;
    }

    @Bean
    public AmqpInboundChannelAdapter amqpInboundChannelAdapter(MessageChannel amqpInboundChannel,
                                                               SimpleMessageListenerContainer amqpContainer) {
        var amqpInboundChannelAdapter = new AmqpInboundChannelAdapter(amqpContainer);
        amqpInboundChannelAdapter.setOutputChannel(amqpInboundChannel); // amqp에서 온 메세지를 brewRequestChannel에 보내줌
        return amqpInboundChannelAdapter;
    }
}
