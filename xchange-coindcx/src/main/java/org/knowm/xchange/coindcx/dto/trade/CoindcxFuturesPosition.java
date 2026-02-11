package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CoindcxFuturesPosition {
  @JsonAlias({"pair", "symbol", "market"})
  private String pair;

  @JsonAlias({"side", "position_side", "position_type"})
  private String side;

  @JsonAlias({"size", "quantity", "position_size", "total_quantity"})
  private BigDecimal size;

  @JsonAlias({"entry_price", "avg_price", "average_price"})
  private BigDecimal entryPrice;

  @JsonAlias({"liquidation_price", "liq_price"})
  private BigDecimal liquidationPrice;

  @JsonAlias({"unrealized_pnl", "unrealised_pnl", "unrealizedPnL"})
  private BigDecimal unrealizedPnl;
}
