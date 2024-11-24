package de.kyle.orderbook.simulation.client;

import de.kyle.orderbook.core.client.OrderbookClient;
import de.kyle.orderbook.core.order.event.OrderExecutionEvent;
import de.kyle.orderbook.core.order.event.OrderPlaceEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RequiredArgsConstructor
public class BotOrderClient implements OrderbookClient {
    private static final Logger log = LoggerFactory.getLogger(BotOrderClient.class);
    private final UUID id;

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public void onRegister() {
        log.info("Bot {} was registered", id);
    }

    @Override
    public void onUnregister() {
        log.info("Bot {} was unregistered", id);
    }

    @Override
    public void onOrderExecutionEvent(OrderExecutionEvent event) {
        if (!event.client().equals(this)) {
            return;
        }
        log.info("Bot {} executed {} {}@{}", this.id, event.type(), event.quantity(), event.value());
    }

    @Override
    public void onOrderPlaceEvent(OrderPlaceEvent event) {

    }
}
