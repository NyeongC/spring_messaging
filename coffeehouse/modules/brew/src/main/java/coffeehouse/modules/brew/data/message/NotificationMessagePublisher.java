package coffeehouse.modules.brew.data.message;

import coffeehouse.modules.brew.domain.OrderId;
import coffeehouse.modules.brew.domain.message.BrewCompletedEvent;
import coffeehouse.modules.brew.domain.service.BrewNotifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessagePublisher implements BrewNotifier {

    // 메세지를 보낼려면 채널이 있어야함
    private final MessageChannel brewCompletedNotifyOrderChannel;
    private final MessageChannel brewCompletedNotifyUserChannel;

    public NotificationMessagePublisher(MessageChannel brewCompletedNotifyOrderChannel, MessageChannel brewCompletedNotifyUserChannel) {
        this.brewCompletedNotifyOrderChannel = brewCompletedNotifyOrderChannel;
        this.brewCompletedNotifyUserChannel = brewCompletedNotifyUserChannel;
    }

    // DTO로 감싸서 메세지 보내주기
    
    @Override
    public void notify(OrderId orderId) {
        var brewCompletedEvent = new BrewCompletedEvent(orderId);
        var message = new GenericMessage<BrewCompletedEvent>(brewCompletedEvent);
        brewCompletedNotifyOrderChannel.send(message);
        brewCompletedNotifyUserChannel.send(message);
    }
}
