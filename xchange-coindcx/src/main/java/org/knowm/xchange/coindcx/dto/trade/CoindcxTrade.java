package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public final class CoindcxTrade {
    public final String orderId;
    public final BigDecimal price;
    public final BigDecimal quantity;
    public final String symbol;
    public final long tradeTime;
    public final boolean isBuyerMarketMaker;
    public final BigDecimal fee;

    public CoindcxTrade(
            @JsonProperty("o") String orderId,
            @JsonProperty("p") BigDecimal price,
            @JsonProperty("q") BigDecimal quantity,
            @JsonProperty("s") String symbol,
            @JsonProperty("T") long tradeTime,
            @JsonProperty("m") boolean isBuyerMarketMaker,
            @JsonProperty("f") BigDecimal fee
    ) {
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.symbol = symbol;
        this.tradeTime = tradeTime;
        this.isBuyerMarketMaker = isBuyerMarketMaker;
        this.fee = fee;
    }
}
