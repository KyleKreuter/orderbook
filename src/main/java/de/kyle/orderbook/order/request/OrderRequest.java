package de.kyle.orderbook.order.request;

import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;

public record OrderRequest(
        ImplicitOrderType implicitOrderType,
        OrderType orderType,
        int quantity,
        float value
) {
}
