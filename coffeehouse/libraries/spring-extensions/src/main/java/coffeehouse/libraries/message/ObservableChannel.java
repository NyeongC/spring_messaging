package coffeehouse.libraries.message;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Observable;

public class ObservableChannel extends Observable implements MessageChannel {

    @Override
    public boolean send(Message<?> message) {
        this.setChanged(); // 메세지채널에 메세지가 오면 관찰자들에게 메세지 줄수있음?
        this.notifyObservers(message);
        return MessageChannel.super.send(message);
    }

    @Override
    public boolean send(Message<?> message, long timeout) {
        return false;
    }
}
