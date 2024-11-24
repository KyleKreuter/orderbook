package de.kyle.orderbook.order;

import de.kyle.orderbook.client.DefaultOrderbookClient;
import de.kyle.orderbook.order.type.ImplicitOrderType;
import de.kyle.orderbook.order.type.OrderType;

import java.time.LocalDateTime;

public record Order(
        DefaultOrderbookClient client,
        ImplicitOrderType implicitOrderType,
        OrderType orderType,
        int quantity,
        float value,
        LocalDateTime time
) implements Comparable<Order> {


    @Override
    public int compareTo(Order o) {
        if (this.implicitOrderType != o.implicitOrderType) {
            throw new IllegalArgumentException("Cannot compare bid to ask order");
        }

        int valueComparison;
        if (this.implicitOrderType.equals(ImplicitOrderType.BID)) {
            valueComparison = Float.compare(o.value, this.value);
        } else {
            valueComparison = Float.compare(this.value, o.value);
        }
        if (valueComparison != 0) {
            return valueComparison;
        }

        int quantityComparison = Integer.compare(this.quantity, o.quantity);
        if (quantityComparison != 0) {
            return quantityComparison;
        }

        return this.time.compareTo(o.time);
    }

}
