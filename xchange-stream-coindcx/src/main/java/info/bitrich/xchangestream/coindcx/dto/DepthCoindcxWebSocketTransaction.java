package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DepthCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
    private final CoindcxOrderbook orderbook;

    public DepthCoindcxWebSocketTransaction(
            @JsonProperty("type") String eventType,
            @JsonProperty("E") long eventTime,
            @JsonProperty("channel") String channel,
            @JsonProperty("b") List<Object[]> bids,
            @JsonProperty("a") List<Object[]> asks
    ) {
        super(eventType, eventTime, channel);
        orderbook = new CoindcxOrderbook(bids, asks);
    }

    public CoindcxOrderbook getOrderbook() {
        return orderbook;
    }
}
