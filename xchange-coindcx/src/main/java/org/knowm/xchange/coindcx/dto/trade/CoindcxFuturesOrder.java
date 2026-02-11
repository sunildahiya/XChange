package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;

@Data
public class CoindcxFuturesOrder {
  private String id;

  @JsonProperty("client_order_id")
  private String clientOrderId;

  private String pair;
  private String side;

  @JsonProperty("order_type")
  private String orderType;

  private String status;

  @JsonProperty("fee_amount")
  private BigDecimal feeAmount;

  @JsonProperty("total_quantity")
  private BigDecimal totalQuantity;

  @JsonProperty("remaining_quantity")
  private BigDecimal remainingQuantity;

  @JsonProperty("avg_price")
  private BigDecimal avgPrice;

  @JsonProperty("price_per_unit")
  private BigDecimal pricePerUnit;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;
}
