package de.kyle.orderbook.client;

import de.kyle.orderbook.order.event.OrderExecutionEvent;
import de.kyle.orderbook.order.event.OrderPlaceEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@RequiredArgsConstructor
public class DefaultOrderbookClient implements OrderbookClient {
    private static final Logger log = LoggerFactory.getLogger(DefaultOrderbookClient.class);
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