package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CoindcxOrderType {
    @JsonProperty("market_order")
    MarketOrder,
    @JsonProperty("limit_order")
    LimitOrder,
    @JsonProperty("stop_limit")
    StopLimit,
    @JsonProperty("take_profit")
    TakeProfit;
}
