package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxAdapters;
import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.coindcx.dto.trade.*;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CoindcxTradeServiceRaw extends CoindcxBaseService {
    protected CoindcxTradeServiceRaw(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    public List<CoindcxOrder> newOrder(
            CurrencyPair pair,
            BigDecimal totalQuantity,
            BigDecimal pricePerUnit,
            CoindcxOrderSide side,
            CoindcxOrderType orderType,
            String clientOrderId
    ) throws IOException {
        CoindcxNewOrderRequest newOrderRequest = new CoindcxNewOrderRequest();
        newOrderRequest.setMarket(CoindcxAdapters.toSymbol(pair));
        newOrderRequest.setTotalQuantity(totalQuantity);
        newOrderRequest.setPricePerUnit(pricePerUnit);
        newOrderRequest.setSide(side);
        newOrderRequest.setOrderType(orderType);
        newOrderRequest.setClientOrderId(clientOrderId);
        newOrderRequest.setTimestamp(getTimestampFactory().createValue());
        return coindcx.newOrder(
                apiKey,
                signatureCreator,
                newOrderRequest
        ).get("orders");
    }

    public boolean cancelOrder(String orderId) throws IOException {
        CoindcxCancelOrderRequest cancelOrderRequest = new CoindcxCancelOrderRequest(
                orderId, null, getTimestampFactory().createValue()
        );
        coindcx.cancelOrder(apiKey, signatureCreator, cancelOrderRequest);
        return true;
    }

    public CoindcxOrder orderStatus(String orderId) throws IOException {
        CoindcxOrderStatusRequest orderStatusRequest = new CoindcxOrderStatusRequest(
                orderId, null, getTimestampFactory().createValue()
        );
        return coindcx.orderStatus(apiKey, signatureCreator, orderStatusRequest);
    }

    public CoindcxOrder editPrice(String orderId, BigDecimal price) throws IOException {
        CoindcxEditPriceRequest editPriceRequest = new CoindcxEditPriceRequest(
                orderId, null, price, getTimestampFactory().createValue()
        );
        return coindcx.editPrice(apiKey, signatureCreator, editPriceRequest);
    }

    public List<Map<Object, Object>> getTradeHistory() throws IOException {
        return coindcx.getTradeHistory(apiKey, signatureCreator, new CoindcxTradeHistoryRequest(1662834600000L, 1662921000000L, getTimestampFactory().createValue()));
    }
}
