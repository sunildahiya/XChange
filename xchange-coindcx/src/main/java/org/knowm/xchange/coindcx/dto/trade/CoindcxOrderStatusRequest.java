package org.knowm.xchange.coindcx.dto.trade;

public class CoindcxOrderStatusRequest extends CoindcxCancelOrderRequest {
    public CoindcxOrderStatusRequest(String id, String clientOrderId, Long timestamp) {
        super(id, clientOrderId, timestamp);
    }
}
