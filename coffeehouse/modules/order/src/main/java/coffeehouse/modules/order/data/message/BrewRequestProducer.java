package coffeehouse.modules.order.data.message;

import coffeehouse.modules.order.domain.OrderId;
import coffeehouse.modules.order.domain.message.BrewRequestCommand;
import coffeehouse.modules.order.domain.service.BarCounter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class BrewRequestProducer implements BarCounter {

    private final MessageChannel barCounterChannel;

    public BrewRequestProducer(MessageChannel barCounterChannel) {
        this.barCounterChannel = barCounterChannel;
    }

    @Override
    public void brew(OrderId orderId) {
        var command = new BrewRequestCommand(orderId);

        /*
        * 스프링 메세지는 메세지를 감싸주기 위해 두 가지 클래스를 제공
        * 1. GenericMessage
        *
        * 2. MessageBuilder
        * */

        // 제네릭 메세지 페이로드 타입이 꼭 필요
        var message = new GenericMessage<BrewRequestCommand>(command);

        barCounterChannel.send(message);

        /*
        * 메세지를 바카운트 채널 보냄
        * 브류 메소드를 통해 orderId가 넘어오면
        * orderId를 BrewRequestCommand로 만들어서
        * BrewRequestCommand 를 이용하여 메세지를 만들고
        * 이 메세지를 barCounterChannel 에 보냄으로써 기존 api 통신으로 요청하는 것이 아닌
        * 메세지 채널을 통하여 보냄
        * */

    }
}
