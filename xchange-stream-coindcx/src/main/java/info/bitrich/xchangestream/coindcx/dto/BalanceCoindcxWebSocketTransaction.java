package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BalanceCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
    private final List<CoindcxBalance> currencyBalances;

    public BalanceCoindcxWebSocketTransaction(
            @JsonProperty("type") String eventType,
            @JsonProperty("channel") String channel,
            @JsonProperty("data") List<CoindcxBalance> currencyBalances
    ) {
        super(eventType, System.currentTimeMillis(), channel);
        this.currencyBalances = currencyBalances;
    }

    public List<CoindcxBalance> getCurrencyBalances() {
        return currencyBalances;
    }
}
