package de.kyle.orderbook.core.order.request;

import de.kyle.orderbook.core.order.type.ImplicitOrderType;
import de.kyle.orderbook.core.order.type.OrderType;

public record OrderRequest(
        ImplicitOrderType implicitOrderType,
        OrderType orderType,
        int quantity,
        float value
) {
}
