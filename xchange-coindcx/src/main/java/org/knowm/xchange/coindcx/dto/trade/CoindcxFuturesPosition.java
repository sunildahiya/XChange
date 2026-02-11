package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CoindcxFuturesPosition {
  private String id;
  private String pair;

  @JsonProperty("active_pos")
  private BigDecimal activePos;

  @JsonProperty("avg_price")
  private BigDecimal avgPrice;

  @JsonProperty("liquidation_price")
  private BigDecimal liquidationPrice;
}
