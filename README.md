
# Orderbook
An Orderbook is a data structure widely used in financial markets to maintain a record of buy and sell orders for a specific asset. It operates by matching bids (buy orders) and asks (sell orders) based on price and quantity, ensuring efficient and fair trade execution.

My Java-based implementation of an Orderbook focuses on real-time order matching and event-driven updates, ensuring high performance and **complete thread safety** for concurrent environments.

## Features
* Thread-Safe Operations: All critical operations are protected by locks, ensuring safe execution in multi-threaded applications.  
* Order Matching: Continuous matching of orders based on defined rules for market and limit orders.  
* Event Handling (PoC): Generates events for order placement and execution, allowing clients to react in real-time.  
* Client Management (PoC): Supports client registration and deregistration with event callbacks.

## How It Works
* [Order](https://github.com/KyleKreuter/orderbook/blob/main/src/main/java/de/kyle/orderbook/order/Order.java): Represents a bid or ask order with details like quantity, price, and order type.  
* [OrderbookClien](https://github.com/KyleKreuter/orderbook/blob/main/src/main/java/de/kyle/orderbook/client/OrderbookClient.java)t: Allows clients to interact with the orderbook, place orders, and listen for events.  
* [AssetTicker](https://github.com/KyleKreuter/orderbook/blob/main/src/main/java/de/kyle/orderbook/asset/AssetTicker.java): Identifies the traded asset.
* Priority Queues: Used for efficient retrieval of the best bid and ask orders.

## Thread Safety
Thread safety is a critical aspect of this implementation, ensuring that all operations on the Orderbook can safely run in a concurrent environment.
#### Key mechanisms include:
* ReentrantLock: Used extensively to protect shared resources like bid/ask queues, client sets, and orderbook state.  
* No Deadlocks: Careful ordering of lock acquisition ensures that the system avoids potential deadlocks.

These mechanisms allow the system to handle multiple clients and orders concurrently without risking data corruption or inconsistent states.

## Testing
The order matching, placement, and thread safety mechanisms have been thoroughly tested. Priority queues correctly handle order precedence, and locks ensure consistent state across all operations.
