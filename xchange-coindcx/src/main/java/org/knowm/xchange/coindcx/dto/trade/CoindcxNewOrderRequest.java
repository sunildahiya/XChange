package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoindcxNewOrderRequest {
    private String market;

    @JsonProperty("total_quantity")
    private BigDecimal totalQuantity;

    @JsonProperty("price_per_unit")
    private BigDecimal pricePerUnit;

    private CoindcxOrderSide side;

    @JsonProperty("order_type")
    private CoindcxOrderType orderType;

    @JsonProperty("client_order_id")
    private String clientOrderId;

    private Long timestamp;
}
