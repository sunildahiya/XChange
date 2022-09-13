package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CoindcxCancelOrderRequest {
    private String id;
    @JsonProperty("client_order_id")
    private String clientOrderId;
    private Long timestamp;

    public CoindcxCancelOrderRequest(String id, String clientOrderId, Long timestamp) {
        this.id = id;
        this.clientOrderId = clientOrderId;
        this.timestamp = timestamp;
    }
}
