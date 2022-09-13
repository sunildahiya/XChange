package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrder;

import java.util.List;

public class OrderUpdateCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
    private final List<CoindcxOrder> orders;

    public OrderUpdateCoindcxWebSocketTransaction(
            @JsonProperty("type") String eventType,
            @JsonProperty("channel") String channel,
            @JsonProperty("data") List<CoindcxOrder> orders
    ) {
        super(eventType, System.currentTimeMillis(), channel);
        this.orders = orders;
    }

    public List<CoindcxOrder> getOrders() {
        return orders;
    }
}
