package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CoindcxOrder {
    private String id;

    @JsonProperty("client_order_id")
    private String clientOrderId;

    private String market;

    private CoindcxOrderSide side;

    @JsonProperty("order_type")
    private CoindcxOrderType orderType;

    private CoindcxOrderStatus status;

    @JsonProperty("fee_amount")
    private BigDecimal feeAmount;

    @JsonProperty("fee")
    private BigDecimal fee;

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
