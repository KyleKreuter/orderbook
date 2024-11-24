package de.kyle.orderbook;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.DefaultOrderbookClient;
import de.kyle.orderbook.order.request.OrderRequest;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class OrderbookLimitOrderMatchingTest {

    @Test
    public void limit_order_part_execution() throws InterruptedException {
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
                100f
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.LMT,
                5,
                99.40f
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.LMT,
                5,
                100.10f
        ));
        Thread.sleep(60);
        Assertions.assertEquals(99.70f, orderbook.getLastTradedPrice());
        Thread.sleep(60);
        Assertions.assertEquals(99.70f, orderbook.getLastTradedPrice());

        orderbook.unregister(firstClient);
        orderbook.unregister(secondClient);
        orderbook.shutdown();
    }

    @Test
    public void limit_order_full_execution() throws InterruptedException {
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
                ImplicitOrderType.ASK,
                OrderType.LMT,
                5,
                99.50f
        ));

        orderbook.place(secondClient, new OrderRequest(
                ImplicitOrderType.ASK,
                OrderType.LMT,
                5,
                99.40f
        ));

        Thread.sleep(60);
        Assertions.assertEquals(99.70f, orderbook.getLastTradedPrice());
        Thread.sleep(60);
        Assertions.assertEquals(99.75f, orderbook.getLastTradedPrice());

        orderbook.unregister(firstClient);
        orderbook.unregister(secondClient);
        orderbook.shutdown();
    }
}
