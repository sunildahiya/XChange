package org.knowm.xchange.dto.marketdata;

import java.util.Objects;

public class OrderbookWithDelta {
    private final OrderBook orderbook;
    private final OrderbookDelta orderbookDelta;

    public OrderbookWithDelta(OrderBook orderbook, OrderbookDelta orderbookDelta) {
        this.orderbook = Objects.requireNonNull(orderbook);
        this.orderbookDelta = Objects.requireNonNull(orderbookDelta);
    }

    public OrderBook getOrderbook() {
        return orderbook;
    }

    public OrderbookDelta getOrderbookDelta() {
        return orderbookDelta;
    }
}
