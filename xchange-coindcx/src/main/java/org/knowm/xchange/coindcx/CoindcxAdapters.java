package org.knowm.xchange.coindcx;

import org.knowm.xchange.coindcx.dto.trade.CoindcxOrder;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderSide;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderStatus;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderType;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.math.BigDecimal;
import java.sql.Date;

public class CoindcxAdapters {
    private CoindcxAdapters() {}

    public static String toSymbol(CurrencyPair pair) {
        return pair.base.getCurrencyCode() + pair.counter.getCurrencyCode();
    }

    public static CurrencyPair adaptSymbol(String symbol) {
        int pairLength = symbol.length();
        if (symbol.endsWith("USDT")) {
            return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDT");
        } else if (symbol.endsWith("USDC")) {
            return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDC");
        } else if (symbol.endsWith("TUSD")) {
            return new CurrencyPair(symbol.substring(0, pairLength - 4), "TUSD");
        } else if (symbol.endsWith("USDS")) {
            return new CurrencyPair(symbol.substring(0, pairLength - 4), "USDS");
        } else if (symbol.endsWith("BUSD")) {
            return new CurrencyPair(symbol.substring(0, pairLength - 4), "BUSD");
        } else {
            return new CurrencyPair(
                    symbol.substring(0, pairLength - 3), symbol.substring(pairLength - 3));
        }
    }

    public static CoindcxOrderSide toSide(Order.OrderType orderType) {
        if (orderType.equals(Order.OrderType.BID))
            return CoindcxOrderSide.buy;
        else if (orderType.equals(Order.OrderType.ASK))
            return CoindcxOrderSide.sell;
        throw new RuntimeException("Unknown orderType " + orderType);
    }

    public static Order adaptOrder(CoindcxOrder order) {
        Order.OrderType type = convert(order.getSide());
        CurrencyPair currencyPair = adaptSymbol(order.getMarket());
        Order.Builder builder;
        if (order.getOrderType().equals(CoindcxOrderType.MarketOrder)) {
            builder = new MarketOrder.Builder(type, currencyPair);
        } else if (order.getOrderType().equals(CoindcxOrderType.LimitOrder)) {
            builder = new LimitOrder.Builder(type, currencyPair).limitPrice(order.getPricePerUnit());
        } else {
            throw new RuntimeException("Unsupported orderType " + order.getOrderType());
        }
        BigDecimal filledQty = order.getTotalQuantity().subtract(order.getRemainingQuantity());
        builder
                .orderStatus(adaptOrderStatus(order.getStatus()))
                .originalAmount(order.getTotalQuantity())
                .id(order.getId())
                .timestamp(Date.from(order.getUpdatedAt()))
                .cumulativeAmount(filledQty)
                .userReference(order.getClientOrderId())
                .averagePrice(order.getAvgPrice());
        return builder.build();
    }

    private static Order.OrderStatus adaptOrderStatus(CoindcxOrderStatus status) {
        switch (status) {
            case Init:
                return Order.OrderStatus.NEW;
            case Open:
                return Order.OrderStatus.OPEN;
            case PartialEntry:
            case PartiallyFilled:
                return Order.OrderStatus.PARTIALLY_FILLED;
            case Filled:
                return Order.OrderStatus.FILLED;
            case PartiallyCancelled:
            case PartialClose:
                return Order.OrderStatus.PARTIALLY_CANCELED;
            case Cancelled:
                return Order.OrderStatus.CANCELED;
            case Rejected:
                return Order.OrderStatus.REJECTED;
            case Close:
                return Order.OrderStatus.CLOSED;
            default:
                throw new RuntimeException("Unknown coindcx orderStatus " + status);
        }
    }

    private static Order.OrderType convert(CoindcxOrderSide side) {
        if (side.equals(CoindcxOrderSide.buy))
            return Order.OrderType.BID;
        else
            return Order.OrderType.ASK;
    }

    public static Order.OrderType convertType(boolean isBuyer) {
        return isBuyer ? Order.OrderType.BID : Order.OrderType.ASK;
    }
}
