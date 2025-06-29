package coffeehouse.modules.brew.data.message;

import coffeehouse.modules.brew.domain.OrderId;
import coffeehouse.modules.brew.domain.service.OrderSheetSubmission;
import coffeehouse.modules.order.domain.message.BrewRequestCommand;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class BrewRequestConsumer {

    private final OrderSheetSubmission orderSheetSubmission;

    public BrewRequestConsumer(OrderSheetSubmission orderSheetSubmission) {
        this.orderSheetSubmission = orderSheetSubmission;
    }


    @ServiceActivator(inputChannel = "brewRequestChannel")
    public void handle(BrewRequestCommand brewRequestCommand) {
        var command = brewRequestCommand;
        var brewOrderId = new OrderId(command.orderId().value());
        orderSheetSubmission.submit(new OrderSheetSubmission.OrderSheetForm(brewOrderId));
    }
}
