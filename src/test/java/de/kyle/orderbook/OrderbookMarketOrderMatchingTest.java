package de.kyle.orderbook;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.DefaultOrderbookClient;
import de.kyle.orderbook.order.request.OrderRequest;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OrderbookMarketOrderMatchingTest {

    @Test
    public void market_order_high_spread() throws InterruptedException {
        DefaultOrderbookClient firstClient = new DefaultOrderbookClient(UUID.randomUUID());
        DefaultOrderbookClient secondClient = new DefaultOrderbookClient(UUID.randomUUID());
        Orderbook orderbook = new Orderbook(AssetTicker.KCJK);
        orderbook.start();
        orderbook.register(firstClient);
        orderbook.register(secondClient);

        orderbook.place(firstClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.MKT,
                10,
                100
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.MKT,
                5,
                50
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.MKT,
                5,
                150
        ));
        Thread.sleep(60);
        Assertions.assertEquals(50, orderbook.getLastTradedPrice());
        Thread.sleep(60);
        Assertions.assertEquals(150, orderbook.getLastTradedPrice());

        orderbook.unregister(firstClient);
        orderbook.unregister(secondClient);
        orderbook.shutdown();
    }

    @Test
    public void market_order_minimal_spread() throws InterruptedException {
        DefaultOrderbookClient firstClient = new DefaultOrderbookClient(UUID.randomUUID());
        DefaultOrderbookClient secondClient = new DefaultOrderbookClient(UUID.randomUUID());
        Orderbook orderbook = new Orderbook(AssetTicker.KCJK);
        orderbook.start();
        orderbook.register(firstClient);
        orderbook.register(secondClient);

        orderbook.place(firstClient, new OrderRequest(
                ImplicitOrderType.BID,
                OrderType.MKT,
                10,
                100
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.MKT,
                5,
                99.9f
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.MKT,
                5,
                100.1f
        ));

        Thread.sleep(60);
        Assertions.assertEquals(99.9f, orderbook.getLastTradedPrice());
        Thread.sleep(60);
        Assertions.assertEquals(100.10f, orderbook.getLastTradedPrice());

        orderbook.unregister(firstClient);
        orderbook.unregister(secondClient);
        orderbook.shutdown();
    }
}