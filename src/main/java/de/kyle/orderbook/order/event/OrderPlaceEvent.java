package de.kyle.orderbook.order.event;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.OrderbookClient;
import de.kyle.orderbook.order.type.ImplicitOrderType;

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
