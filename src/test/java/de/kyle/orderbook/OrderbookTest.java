package de.kyle.orderbook;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.DefaultOrderbookClient;
import de.kyle.orderbook.order.request.OrderRequest;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OrderbookTest {

    @Test
    public void test_matching_limit_order() throws InterruptedException {
        DefaultOrderbookClient orderClient = new DefaultOrderbookClient(UUID.randomUUID());
        Orderbook orderbook = new Orderbook(AssetTicker.KCJK);
        orderbook.start();
        orderbook.register(orderClient);
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.LMT,
                5,
                100
        ));
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.MKT,
                30,
                110
        ));
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.LMT,
                10,
                105
        ));
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.MKT,
                20,
                120
        ));
        Thread.sleep(150);
        Assertions.assertEquals(0, orderbook.getAskQueue().size());
        Assertions.assertEquals(1, orderbook.getBidQueue().size());
        orderbook.unregister(orderClient);
        orderbook.shutdown();
    }
}
