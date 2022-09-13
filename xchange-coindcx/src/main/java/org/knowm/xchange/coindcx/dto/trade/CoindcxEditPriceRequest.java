package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoindcxEditPriceRequest {
    private String id;
    @JsonProperty("client_order_id")
    private String clientOrderId;
    @JsonProperty("price_per_unit")
    private BigDecimal pricePerUnit;
    private long timestamp;

    public CoindcxEditPriceRequest(String id, String clientOrderId, BigDecimal pricePerUnit, long timestamp) {
        this.id = id;
        this.clientOrderId = clientOrderId;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
    }
}
