package de.kyle.orderbook;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.DefaultOrderbookClient;
import de.kyle.orderbook.order.Order;
import de.kyle.orderbook.order.request.OrderRequest;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OrderbookOrderByTimeTest {

    @Test
    public void order_by_entry_time_on_equal_value_and_quantity() {
        DefaultOrderbookClient firstClient = new DefaultOrderbookClient(UUID.randomUUID());
        DefaultOrderbookClient secondClient = new DefaultOrderbookClient(UUID.randomUUID());
        Orderbook orderbook = new Orderbook(AssetTicker.KCJK);
        orderbook.start();
        orderbook.register(firstClient);
        orderbook.register(secondClient);

        orderbook.place(firstClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.LMT,
                10,
                100
        ));
        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.LMT,
                10,
                100
        ));

        Order firstOrder = orderbook.getBidQueue().peek();
        Assertions.assertNotNull(firstOrder);
        Assertions.assertEquals(firstClient.getId(), firstOrder.client().getId());

        orderbook.unregister(firstClient);
        orderbook.unregister(secondClient);
        orderbook.shutdown();
    }
}
