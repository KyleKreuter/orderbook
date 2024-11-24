package de.kyle.orderbook;

import de.kyle.orderbook.asset.AssetTicker;
import de.kyle.orderbook.client.OrderbookClient;
import de.kyle.orderbook.order.Order;
import de.kyle.orderbook.order.event.OrderExecutionEvent;
import de.kyle.orderbook.order.event.OrderPlaceEvent;
import de.kyle.orderbook.order.request.OrderRequest;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Orderbook {
    private static final Logger log = LoggerFactory.getLogger(Orderbook.class);
    private final AssetTicker ticker;
    @Getter
    private final PriorityQueue<Order> bidQueue;
    @Getter
    private final PriorityQueue<Order> askQueue;

    private final Set<OrderbookClient> clients;

    private final Lock bidLock = new ReentrantLock();
    private final Lock askLock = new ReentrantLock();
    private final Lock clientLock = new ReentrantLock();
    private final Lock orderbookAliveLock = new ReentrantLock();
    private final Lock lastTradedPriceLock = new ReentrantLock();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean orderbookAlive;
    private float lastTradedPrice;

    public Orderbook(AssetTicker ticker) {
        this.ticker = ticker;

        this.bidQueue = new PriorityQueue<>(Order::compareTo);
        this.askQueue = new PriorityQueue<>(Order::compareTo);

        this.clients = new HashSet<>();
        log.info("Orderbook was initiated");
        log.info("Waiting to start the order matching...");
    }

    public float getLastTradedPrice() {
        lastTradedPriceLock.lock();
        try {
            return lastTradedPrice;
        } finally {
            lastTradedPriceLock.unlock();
        }
    }

    private void setLastTradedPrice(float price) {
        lastTradedPriceLock.lock();
        try {
            lastTradedPrice = price;
        } finally {
            lastTradedPriceLock.unlock();
        }
    }

    public void register(OrderbookClient client) {
        if (!isOrderbookAlive()) {
            throw new IllegalStateException("Orderbook is not alive; Start the orderbook to register a client");
        }
        this.clientLock.lock();
        try {
            this.clients.add(client);
        } finally {
            this.clientLock.unlock();
        }
        log.info("Registered client {}", client.getId());
        client.onRegister();
    }

    public void unregister(OrderbookClient client) {
        if (!isOrderbookAlive()) {
            throw new IllegalStateException("Orderbook is not alive; Start the orderbook to unregister a client");
        }
        this.clientLock.lock();
        try {
            this.clients.remove(client);
        } finally {
            this.clientLock.unlock();
        }
        log.info("Unregistered client {}", client.getId());
        client.onUnregister();
    }

    public void place(OrderbookClient client, OrderRequest orderRequest) {
        if (!isOrderbookAlive()) {
            throw new IllegalStateException("Orderbook is not alive; Start the orderbook to place an order");
        }
        clientLock.lock();
        try {
            if (!this.clients.contains(client)) {
                throw new IllegalCallerException("Cannot place order when not registered");
            }
        } finally {
            clientLock.unlock();
        }
        LocalDateTime orderTime = LocalDateTime.now();
        Order order = new Order(
                client,
                orderRequest.implicitOrderType(),
                orderRequest.orderType(),
                orderRequest.quantity(),
                orderRequest.value(),
                orderTime
        );
        switch (orderRequest.implicitOrderType()) {
            case ASK -> {
                this.askLock.lock();
                try {
                    this.askQueue.add(order);
                } finally {
                    this.askLock.unlock();
                }
                log.info("ASK order for {}@{} was placed by {}", order.quantity(), order.value(), client.getId());
                callOrderPlaceEvent(new OrderPlaceEvent(
                        client,
                        orderRequest.implicitOrderType(),
                        this.ticker,
                        order.quantity(),
                        order.value(),
                        orderTime
                ));
                return;
            }
            case BID -> {
                this.bidLock.lock();
                try {
                    this.bidQueue.add(order);
                } finally {
                    this.bidLock.unlock();
                }
                log.info("BID order for {}@{} was placed by {}", order.quantity(), order.value(), client.getId());
                callOrderPlaceEvent(new OrderPlaceEvent(
                        client,
                        orderRequest.implicitOrderType(),
                        this.ticker,
                        order.quantity(),
                        order.value(),
                        orderTime
                ));
                return;
            }
        }
        throw new IllegalArgumentException("Orderbook supports only ask and bid orders");
    }

    public void match() {
        this.bidLock.lock();
        this.askLock.lock();
        try {
            Order bidPeek = this.bidQueue.peek();
            Order askPeek = this.askQueue.peek();

            if (bidPeek == null || askPeek == null) {
                return;
            }

            if (!canMatch(bidPeek, askPeek)) {
                return;
            }

            processExecution(bidPeek, askPeek);
        } finally {
            this.bidLock.unlock();
            this.askLock.unlock();
        }
    }

    private boolean isOrderbookAlive() {
        this.orderbookAliveLock.lock();
        try {
            return this.orderbookAlive;
        } finally {
            this.orderbookAliveLock.unlock();
        }
    }

    private void setOrderbookAlive(boolean running) {
        this.orderbookAliveLock.lock();
        try {
            this.orderbookAlive = running;
        } finally {
            this.orderbookAliveLock.unlock();
        }
    }

    public void start() {
        if (isOrderbookAlive()) {
            throw new IllegalStateException("Orderbook is already alive");
        }
        setOrderbookAlive(true);
        log.info("Order matching was started");
        scheduler.scheduleAtFixedRate(() -> {
            if (isOrderbookAlive()) {
                match();
            } else {
                scheduler.shutdown();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        setOrderbookAlive(false);
        this.bidLock.lock();
        this.askLock.lock();
        this.clientLock.lock();
        try {
            this.bidQueue.clear();
            this.askQueue.clear();
            for (OrderbookClient client : this.clients) {
                client.onUnregister();
            }
            this.clients.clear();
        } finally {
            this.bidLock.unlock();
            this.askLock.unlock();
            this.clientLock.unlock();
        }
        log.info("All bid/ask orders and clients were removed");
        log.info("Orderbook is shut down");
    }

    private void callOrderPlaceEvent(OrderPlaceEvent event) {
        this.clientLock.lock();
        try {
            for (OrderbookClient client : this.clients) {
                client.onOrderPlaceEvent(event);
            }
        } finally {
            this.clientLock.unlock();
        }
    }

    private void callOrderExecutionEvent(OrderExecutionEvent event) {
        this.clientLock.lock();
        try {
            for (OrderbookClient client : this.clients) {
                client.onOrderExecutionEvent(event);
            }
        } finally {
            this.clientLock.unlock();
        }
    }

    private float getExecutionPrice(Order bid, Order ask) {
        if (bid.orderType().equals(OrderType.MKT)) {
            return ask.value();
        }
        if (ask.orderType().equals(OrderType.MKT)) {
            return bid.value();
        }
        return (bid.value() + ask.value()) / 2;
    }

    private boolean canMatch(Order bid, Order ask) {
        if (bid.orderType().equals(OrderType.LMT) && ask.orderType().equals(OrderType.LMT)) {
            return bid.value() >= ask.value();
        }
        return true;
    }

    private void processExecution(Order bid, Order ask) {
        int bidQuantity = bid.quantity();
        int askQuantity = ask.quantity();
        float executionPrice = getExecutionPrice(bid, ask);
        setLastTradedPrice(executionPrice);
        if (bidQuantity > askQuantity) {
            processPartExecution(
                    bid,
                    ask,
                    bidQuantity,
                    askQuantity,
                    executionPrice,
                    this.bidQueue,
                    bid.client(),
                    bid.implicitOrderType(),
                    bid.orderType(),
                    bid.value()
            );
        } else if (bidQuantity < askQuantity) {
            processPartExecution(bid,
                    ask,
                    askQuantity,
                    bidQuantity,
                    executionPrice,
                    this.askQueue,
                    ask.client(),
                    ask.implicitOrderType(),
                    ask.orderType(),
                    ask.value()
            );
        } else {
            processFullExecution(bid, ask, bidQuantity, executionPrice, askQuantity);
        }
    }

    private void processFullExecution(Order bid, Order ask, int bidQuantity, float executionPrice, int askQuantity) {
        this.bidQueue.poll();
        this.askQueue.poll();
        callOrderExecutionEvent(new OrderExecutionEvent(
                bid.client(),
                bid.implicitOrderType(),
                this.ticker,
                bidQuantity,
                executionPrice,
                LocalDateTime.now()
        ));
        callOrderExecutionEvent(new OrderExecutionEvent(
                ask.client(),
                ask.implicitOrderType(),
                this.ticker,
                askQuantity,
                executionPrice,
                LocalDateTime.now()
        ));
    }

    private void processPartExecution(Order bid,
                                      Order ask,
                                      int bidQuantity,
                                      int askQuantity,
                                      float executionPrice,
                                      PriorityQueue<Order> bidQueue,
                                      OrderbookClient client,
                                      ImplicitOrderType implicitOrderType,
                                      OrderType orderType,
                                      float value) {
        this.bidQueue.poll();
        this.askQueue.poll();
        callOrderExecutionEvent(new OrderExecutionEvent(
                bid.client(),
                bid.implicitOrderType(),
                this.ticker,
                askQuantity,
                executionPrice,
                LocalDateTime.now()
        ));
        callOrderExecutionEvent(new OrderExecutionEvent(
                ask.client(),
                ask.implicitOrderType(),
                this.ticker,
                askQuantity,
                executionPrice,
                LocalDateTime.now()
        ));
        bidQueue.add(new Order(
                client,
                implicitOrderType,
                orderType,
                bidQuantity - askQuantity,
                value,
                LocalDateTime.now()
        ));
    }
}