package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesOrder;

public class FuturesOrderUpdateCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
  private final List<CoindcxFuturesOrder> orders;

  public FuturesOrderUpdateCoindcxWebSocketTransaction(
      @JsonProperty("type") String eventType,
      @JsonProperty("channel") String channel,
      @JsonProperty("data") List<CoindcxFuturesOrder> orders) {
    super(eventType, System.currentTimeMillis(), channel);
    this.orders = orders;
  }

  public List<CoindcxFuturesOrder> getOrders() {
    return orders;
  }
}
