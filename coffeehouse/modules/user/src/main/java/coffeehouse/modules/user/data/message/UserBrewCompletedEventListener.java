package coffeehouse.modules.user.data.message;

import coffeehouse.modules.user.data.message.dto.BrewCompletedEvent;
import coffeehouse.modules.user.domain.service.UserBrewCompleted;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class UserBrewCompletedEventListener {

    private final UserBrewCompleted userBrewCompleted;

    public UserBrewCompletedEventListener(UserBrewCompleted userBrewCompleted) {
        this.userBrewCompleted = userBrewCompleted;
    }


    @ServiceActivator(inputChannel = "brewCompletedEventPublishSubscribeChannel")
    public void handle(BrewCompletedEvent brewCompletedEvent) {
        userBrewCompleted.notify(brewCompletedEvent.orderId());
    }
}
