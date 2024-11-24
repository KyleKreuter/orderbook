package de.kyle.orderbook.core.order.event;

import de.kyle.orderbook.core.asset.AssetTicker;
import de.kyle.orderbook.core.client.OrderbookClient;
import de.kyle.orderbook.core.order.type.ImplicitOrderType;

import java.time.LocalDateTime;

public record OrderPlaceEvent(
        OrderbookClient client,
        ImplicitOrderType type,
        AssetTicker ticker,
        int quantity,
        float value,
        LocalDateTime time
) {
}
