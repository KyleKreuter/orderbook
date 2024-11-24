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
    public void test_matching_limit_order() {
        DefaultOrderbookClient orderClient = new DefaultOrderbookClient(UUID.randomUUID());
        Orderbook orderbook = new Orderbook(AssetTicker.KCJK);
        orderbook.register(orderClient);
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.LMT,
                5,
                100
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
        orderbook.place(orderClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.MKT,
                30,
                110
        ));
        orderbook.match();
        Assertions.assertEquals(1, orderbook.getAskQueue().size());
        Assertions.assertEquals(2, orderbook.getBidQueue().size());
        orderbook.shutdown();
    }
}
