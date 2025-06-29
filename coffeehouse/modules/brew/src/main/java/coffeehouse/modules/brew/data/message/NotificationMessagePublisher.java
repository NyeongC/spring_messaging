package coffeehouse.modules.brew.data.message;

import coffeehouse.modules.brew.domain.OrderId;
import coffeehouse.modules.brew.domain.message.BrewCompletedEvent;
import coffeehouse.modules.brew.domain.service.BrewNotifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessagePublisher implements BrewNotifier {

    private final MessageChannel brewCompletedChannel;

    public NotificationMessagePublisher(MessageChannel brewCompletedChannel) {
        this.brewCompletedChannel = brewCompletedChannel;
    }

    // DTO로 감싸서 메세지 보내주기
    @Override
    public void notify(OrderId orderId) {
        var brewCompletedEvent = new BrewCompletedEvent(orderId);
        var message = new GenericMessage<BrewCompletedEvent>(brewCompletedEvent);
        brewCompletedChannel.send(message);
    }
}
