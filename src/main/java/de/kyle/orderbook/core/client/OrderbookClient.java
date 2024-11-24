package de.kyle.orderbook.core.client;

import de.kyle.orderbook.core.order.event.OrderExecutionEvent;
import de.kyle.orderbook.core.order.event.OrderPlaceEvent;

import java.util.UUID;

public interface OrderbookClient {
    UUID getId();

    void onRegister();

    void onUnregister();

    void onOrderExecutionEvent(OrderExecutionEvent event);

    void onOrderPlaceEvent(OrderPlaceEvent event);
}
