package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesPosition;

public class PositionUpdateCoindcxWebSocketTransaction extends BaseCoindcxWebSocketTransaction {
  private final List<CoindcxFuturesPosition> positions;

  public PositionUpdateCoindcxWebSocketTransaction(
      @JsonProperty("type") String eventType,
      @JsonProperty("channel") String channel,
      @JsonProperty("data") List<CoindcxFuturesPosition> positions) {
    super(eventType, System.currentTimeMillis(), channel);
    this.positions = positions;
  }

  public List<CoindcxFuturesPosition> getPositions() {
    return positions;
  }
}
