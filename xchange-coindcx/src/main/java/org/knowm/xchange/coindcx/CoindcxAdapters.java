package org.knowm.xchange.coindcx;

import org.knowm.xchange.coindcx.dto.trade.CoindcxOrder;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderSide;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderStatus;
import org.knowm.xchange.coindcx.dto.trade.CoindcxOrderType;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesOrder;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesPosition;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Locale;

public class CoindcxAdapters {
    private CoindcxAdapters() {}

    public static String toSymbol(CurrencyPair pair) {
        return pair.base.getCurrencyCode() + pair.counter.getCurrencyCode();
    }

    public static CurrencyPair adaptSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new RuntimeException("Invalid symbol: " + symbol);
        }

        String normalized = symbol.trim();
        int atIndex = normalized.indexOf('@');
        if (atIndex > -1) {
            normalized = normalized.substring(0, atIndex);
        }
        if (normalized.endsWith("-futures")) {
            normalized = normalized.substring(0, normalized.length() - "-futures".length());
        }

        int dashIndex = normalized.indexOf('-');
        if (dashIndex > -1) {
            normalized = normalized.substring(dashIndex + 1);
        }

        if (normalized.contains("_")) {
            String[] parts = normalized.split("_", 2);
            if (parts.length == 2) {
                return new CurrencyPair(parts[0], parts[1]);
            }
        }

        int pairLength = normalized.length();
        if (normalized.endsWith("USDT")) {
            return new CurrencyPair(normalized.substring(0, pairLength - 4), "USDT");
        } else if (normalized.endsWith("USDC")) {
            return new CurrencyPair(normalized.substring(0, pairLength - 4), "USDC");
        } else if (normalized.endsWith("TUSD")) {
            return new CurrencyPair(normalized.substring(0, pairLength - 4), "TUSD");
        } else if (normalized.endsWith("USDS")) {
            return new CurrencyPair(normalized.substring(0, pairLength - 4), "USDS");
        } else if (normalized.endsWith("BUSD")) {
            return new CurrencyPair(normalized.substring(0, pairLength - 4), "BUSD");
        } else {
            return new CurrencyPair(
                    normalized.substring(0, pairLength - 3), normalized.substring(pairLength - 3));
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

    public static Order adaptOrder(CoindcxFuturesOrder order) {
        Order.OrderType type = convertSide(order.getSide());
        CurrencyPair currencyPair = adaptSymbol(order.getPair());
        Order.Builder builder;
        if (isMarketOrder(order.getOrderType())) {
            builder = new MarketOrder.Builder(type, currencyPair);
        } else {
            builder = new LimitOrder.Builder(type, currencyPair).limitPrice(order.getPricePerUnit());
        }
        BigDecimal filledQty = getFilledQty(order.getTotalQuantity(), order.getRemainingQuantity());
        builder
                .orderStatus(adaptOrderStatus(order.getStatus()))
                .originalAmount(order.getTotalQuantity())
                .id(order.getId())
                .timestamp(Date.from(timestampOf(order.getUpdatedAt(), order.getCreatedAt())))
                .cumulativeAmount(filledQty)
                .userReference(order.getClientOrderId())
                .averagePrice(order.getAvgPrice())
                .fee(order.getFeeAmount());
        return builder.build();
    }

    public static OpenPosition adaptOpenPosition(CoindcxFuturesPosition position) {
        BigDecimal activePos = position.getActivePos();
        return new OpenPosition.Builder()
                .instrument(adaptSymbol(position.getPair()))
                .type(adaptPositionType(activePos))
                .size(activePos == null ? null : activePos.abs())
                .price(position.getAvgPrice())
                .liquidationPrice(position.getLiquidationPrice())
                .unRealisedPnl(null)
                .build();
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
            case Triggered:
                return Order.OrderStatus.STOPPED;
            default:
                throw new RuntimeException("Unknown coindcx orderStatus " + status);
        }
    }

    private static Order.OrderStatus adaptOrderStatus(String status) {
        if (status == null) {
            return Order.OrderStatus.UNKNOWN;
        }
        switch (status.toLowerCase(Locale.ROOT)) {
            case "init":
                return Order.OrderStatus.NEW;
            case "open":
                return Order.OrderStatus.OPEN;
            case "partial_entry":
            case "partially_filled":
                return Order.OrderStatus.PARTIALLY_FILLED;
            case "filled":
                return Order.OrderStatus.FILLED;
            case "partially_cancelled":
            case "partial_close":
                return Order.OrderStatus.PARTIALLY_CANCELED;
            case "cancelled":
                return Order.OrderStatus.CANCELED;
            case "rejected":
                return Order.OrderStatus.REJECTED;
            case "close":
                return Order.OrderStatus.CLOSED;
            case "triggered":
                return Order.OrderStatus.STOPPED;
            case "expired":
                return Order.OrderStatus.EXPIRED;
            default:
                return Order.OrderStatus.UNKNOWN;
        }
    }

    private static Order.OrderType convert(CoindcxOrderSide side) {
        if (side.equals(CoindcxOrderSide.buy))
            return Order.OrderType.BID;
        else
            return Order.OrderType.ASK;
    }

    private static Order.OrderType convertSide(String side) {
        return side.equalsIgnoreCase("buy") ? Order.OrderType.BID : Order.OrderType.ASK;
    }

    private static OpenPosition.Type adaptPositionType(BigDecimal activePos) {
        if (activePos == null) {
            return OpenPosition.Type.LONG;
        }
        if (activePos.signum() < 0) {
            return OpenPosition.Type.SHORT;
        }
        return OpenPosition.Type.LONG;
    }

    private static boolean isMarketOrder(String orderType) {
        return orderType != null && orderType.toLowerCase(Locale.ROOT).contains("market");
    }

    private static BigDecimal getFilledQty(BigDecimal totalQuantity, BigDecimal remainingQuantity) {
        return totalQuantity.subtract(remainingQuantity);
    }

    private static Instant timestampOf(Instant updatedAt, Instant createdAt) {
        if (updatedAt != null) {
            return updatedAt;
        }
        if (createdAt != null) {
            return createdAt;
        }
        return Instant.now();
    }

    public static Order.OrderType convertType(boolean isBuyer) {
        return isBuyer ? Order.OrderType.BID : Order.OrderType.ASK;
    }
}
