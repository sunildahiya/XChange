package org.knowm.xchange.dto.marketdata;

import org.knowm.xchange.dto.trade.LimitOrder;

import java.time.Instant;
import java.util.List;

public class OrderbookDelta {
    private long eventTime;
    private long receivedTime;
    private List<LimitOrder> bids;
    private List<LimitOrder> asks;
    private boolean isSnapshot;

    public OrderbookDelta(long eventTime, long receivedTime, List<LimitOrder> bids, List<LimitOrder> asks, boolean isSnapshot) {
        this.eventTime = eventTime;
        this.receivedTime = receivedTime;
        this.bids = bids;
        this.asks = asks;
        this.isSnapshot = isSnapshot;
    }
}
