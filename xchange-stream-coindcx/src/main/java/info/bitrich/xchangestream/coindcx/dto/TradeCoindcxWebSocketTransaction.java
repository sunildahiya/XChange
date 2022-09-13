package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class TradeCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final String symbol;
    private final boolean isBuyerMarketMaker;

    public TradeCoindcxWebSocketTransaction(
            @JsonProperty("type") String eventType,
            @JsonProperty("T") long tradeTime,
            @JsonProperty("channel") String channel,
            @JsonProperty("p") BigDecimal price,
            @JsonProperty("q") BigDecimal quantity,
            @JsonProperty("s") String symbol,
            @JsonProperty("m") boolean isBuyerMarketMaker
    ) {
        super(eventType, tradeTime, channel);
        this.price = price;
        this.quantity = quantity;
        this.symbol = symbol;
        this.isBuyerMarketMaker = isBuyerMarketMaker;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isBuyerMarketMaker() {
        return isBuyerMarketMaker;
    }
}
