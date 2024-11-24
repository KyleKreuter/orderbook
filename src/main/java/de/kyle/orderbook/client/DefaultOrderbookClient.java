package de.kyle.orderbook.client;

import de.kyle.orderbook.order.event.OrderExecutionEvent;
import de.kyle.orderbook.order.event.OrderPlaceEvent;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@RequiredArgsConstructor
public class DefaultOrderbookClient implements OrderbookClient {
    private static final Logger log = LogManager.getLogger(DefaultOrderbookClient.class);
    private final UUID id;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void onRegister() {
        log.info("DefaultOrderbookClient {} was registered", id.toString());
    }

    @Override
    public void onUnregister() {
        log.info("DefaultOrderbookClient {} was unregistered", id.toString());
    }

    @Override
    public void onOrderExecutionEvent(OrderExecutionEvent event) {
        if (!event.client().equals(this)) {
            return;
        }
        log.info("{} {}@{} was successful", event.type(), event.quantity(), event.value());
    }

    @Override
    public void onOrderPlaceEvent(OrderPlaceEvent event) {
        if (!event.client().equals(this)) {
            return;
        }
        log.info("{} {}@{} was placed", event.type(), event.quantity(), event.value());
    }
}