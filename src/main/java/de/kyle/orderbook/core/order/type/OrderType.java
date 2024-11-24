package de.kyle.orderbook.core.order.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    MKT("Market Order"),
    LMT("Limit Order");
    private final String qualifiedName;
}
