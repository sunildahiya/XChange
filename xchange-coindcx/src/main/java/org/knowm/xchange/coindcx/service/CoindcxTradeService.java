package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxAdapters;
import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrder;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderType;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultCancelOrderParamId;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParams;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CoindcxTradeService extends CoindcxTradeServiceRaw implements TradeService {

    public CoindcxTradeService(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
        List<CoindcxOrder> orderResponse = newOrder(
                limitOrder.getCurrencyPair(),
                limitOrder.getOriginalAmount(),
                limitOrder.getLimitPrice(),
                CoindcxAdapters.toSide(limitOrder.getType()),
                CoindcxOrderType.LimitOrder,
                limitOrder.getUserReference()
        );
        assert orderResponse.size() == 1;

        return orderResponse.get(0).getId();
    }

    @Override
    public Collection<Order> getOrder(OrderQueryParams... orderQueryParams) throws IOException {
        return Collections.singletonList(CoindcxAdapters.adaptOrder(orderStatus(orderQueryParams[0].getOrderId())));
    }

    @Override
    public String changeOrder(LimitOrder limitOrder) throws IOException {
        editPrice(limitOrder.getId(), limitOrder.getLimitPrice());
        return limitOrder.getId();
    }

    @Override
    public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
        return cancelOrder(((DefaultCancelOrderParamId) orderParams).getOrderId());
    }
}
