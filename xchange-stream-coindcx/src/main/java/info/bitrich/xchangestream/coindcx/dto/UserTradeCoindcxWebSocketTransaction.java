package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.coindcx.dto.trade.CoindcxTrade;

import java.util.List;

public class UserTradeCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
    private final List<CoindcxTrade> trades;

    public UserTradeCoindcxWebSocketTransaction(
            @JsonProperty("type") String eventType,
            @JsonProperty("channel") String channel,
            @JsonProperty("data") List<CoindcxTrade> trades
    ) {
        super(eventType, System.currentTimeMillis(), channel);
        this.trades = trades;
    }

    public List<CoindcxTrade> getTrades() {
        return trades;
    }
}
