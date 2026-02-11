package info.bitrich.xchangestream.coindcx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.coindcx.dto.BaseCoindcxWebSocketTransaction.CoindcxWebSocketType;
import info.bitrich.xchangestream.coindcx.dto.CoindcxOrderbook;
import info.bitrich.xchangestream.coindcx.dto.DepthCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.coindcx.dto.TradeCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.knowm.xchange.coindcx.CoindcxAdapters;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CoindcxStreamingMarketDataService implements StreamingMarketDataService {
    private final Map<String, Observable<OrderBook>> orderbookSubscriptions;
    private final CoindcxStreamingService service;
    private final Map<String, Observable<DepthCoindcxWebSocketTransaction>> orderbookRawUpdatesSubscriptions;
    private final Map<String, Observable<TradeCoindcxWebSocketTransaction>> tradeSubscriptions;
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();
    public CoindcxStreamingMarketDataService(CoindcxStreamingService streamingService) {
        this.service = streamingService;
        this.orderbookSubscriptions = new ConcurrentHashMap<>();
        this.orderbookRawUpdatesSubscriptions = new ConcurrentHashMap<>();
        this.tradeSubscriptions = new ConcurrentHashMap<>();
    }

    public void openSubscriptions(ProductSubscription productSubscription) {
        productSubscription.getOrderBook().forEach(this::initRawOrderBookUpdatesSubscription);
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        CoindcxMarketType marketType = getMarketType(args);
        String key = key(currencyPair, marketType);
        return orderbookSubscriptions.computeIfAbsent(key, ignore -> initOrderbookIfAbsent(currencyPair, marketType));
    }

    @Override
    public Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
        if (instrument instanceof FuturesContract) {
            return getFuturesOrderBook((FuturesContract) instrument);
        }
        if (instrument instanceof CurrencyPair) {
            return getOrderBook((CurrencyPair) instrument, args);
        }
        throw new NotYetImplementedForExchangeException("getOrderBook");
    }

    @Override
    public Observable<OrderBook> getOrderbookChanges(CurrencyPair currencyPair, Object... args) {
        return getOrderBook(currencyPair, args);
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        CoindcxMarketType marketType = getMarketType(args);
        String key = key(currencyPair, marketType);
        return tradeSubscriptions.computeIfAbsent(key, ignore -> initTradeSubscription(currencyPair, marketType))
                .map(rawTrade ->
                    new Trade.Builder()
                            .type(CoindcxAdapters.convertType(rawTrade.isBuyerMarketMaker()))
                            .originalAmount(rawTrade.getQuantity())
                            .instrument(currencyPair)
                            .price(rawTrade.getPrice())
                            .timestamp(new Date(rawTrade.getEventTime()))
                            .build()
                );
    }

    public Observable<OrderBook> getFuturesOrderBook(CurrencyPair currencyPair) {
        return getOrderBook(currencyPair, CoindcxMarketType.FUTURES);
    }

    public Observable<OrderBook> getFuturesOrderBook(FuturesContract futuresContract) {
        CurrencyPair currencyPair = futuresContract.getCurrencyPair();
        String key = key(futuresContract);
        return orderbookSubscriptions.computeIfAbsent(
                key, ignore -> initOrderbookIfAbsent(currencyPair, CoindcxMarketType.FUTURES));
    }

    public Observable<Trade> getFuturesTrades(CurrencyPair currencyPair) {
        return getTrades(currencyPair, CoindcxMarketType.FUTURES);
    }

    private Observable<OrderBook> initOrderbookIfAbsent(CurrencyPair currencyPair, CoindcxMarketType marketType) {
        String key = key(currencyPair, marketType);
        orderbookRawUpdatesSubscriptions.computeIfAbsent(key, ignore -> triggerObservableBody(rawOrderBookUpdates(currencyPair, marketType)));
        return orderbookRawUpdatesSubscriptions.get(key)
                .map(transaction -> {
                    CoindcxOrderbook coindcxOrderbook = transaction.getOrderbook();
                    List<LimitOrder> bids =
                            coindcxOrderbook.bids.entrySet().stream().map( priceAndQty
                                    -> new LimitOrder(Order.OrderType.BID, priceAndQty.getValue(), currencyPair, null, null, priceAndQty.getKey())
                            ).collect(Collectors.toList());

                    List<LimitOrder> asks =
                            coindcxOrderbook.asks.entrySet().stream().map( priceAndQty
                                    -> new LimitOrder(Order.OrderType.ASK, priceAndQty.getValue(), currencyPair, null, null, priceAndQty.getKey())
                            ).collect(Collectors.toList());
                    return new OrderBook(currencyPair, null, asks, bids);
                });
    }

    private void initRawOrderBookUpdatesSubscription(CurrencyPair currencyPair) {
        orderbookRawUpdatesSubscriptions.put(
                key(currencyPair, CoindcxMarketType.SPOT), triggerObservableBody(rawOrderBookUpdates(currencyPair, CoindcxMarketType.SPOT)));
    }

    private Observable<TradeCoindcxWebSocketTransaction> initTradeSubscription(CurrencyPair currencyPair, CoindcxMarketType marketType) {
        return triggerObservableBody(
                 service.subscribeChannel(channelFromCurrency(currencyPair, marketType, CoindcxWebSocketType.NewTrade))
                        .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.NewTrade.getSerializedValue()))
                        .map(it -> this.readTransaction(it, new TypeReference<TradeCoindcxWebSocketTransaction>() {}, CoindcxWebSocketType.NewTrade))
        );
    }

    private Observable<DepthCoindcxWebSocketTransaction> rawOrderBookUpdates(
            CurrencyPair currencyPair, CoindcxMarketType marketType) {
        return service
                .subscribeChannel(channelFromCurrency(currencyPair, marketType, CoindcxWebSocketType.DepthSnapshot))
                .filter(message -> isOrderBookEvent(message.get("type").asText(), marketType))
                .map(
                        it ->
                                this.readTransaction(
                                        it, new TypeReference<DepthCoindcxWebSocketTransaction>(){}, CoindcxWebSocketType.DepthSnapshot));
    }

    private String channelFromCurrency(CurrencyPair currencyPair, CoindcxMarketType marketType, CoindcxWebSocketType eventType) {
        if (marketType == CoindcxMarketType.FUTURES) {
            String symbol = String.format("B-%s_%s", currencyPair.base, currencyPair.counter);
            if (eventType == CoindcxWebSocketType.NewTrade) {
                return symbol + "@trades-futures";
            }
            return symbol + "@orderbook@50-futures";
        }
        // We are supporting only coindcx native markets
        String eCode = "I";
        if (!currencyPair.counter.getCurrencyCode().equals("INR"))
            eCode = "KC";
        return String.format("%s-%s_%s", eCode, currencyPair.base, currencyPair.counter);
    }

    private CoindcxMarketType getMarketType(Object... args) {
        if (args != null && args.length > 0 && args[0] instanceof CoindcxMarketType) {
            return (CoindcxMarketType) args[0];
        }
        return CoindcxMarketType.SPOT;
    }

    private String key(CurrencyPair currencyPair, CoindcxMarketType marketType) {
        return marketType.name() + ":" + currencyPair.base + ":" + currencyPair.counter;
    }

    private String key(FuturesContract futuresContract) {
        CurrencyPair pair = futuresContract.getCurrencyPair();
        return CoindcxMarketType.FUTURES.name()
                + ":"
                + pair.base
                + ":"
                + pair.counter
                + ":"
                + futuresContract.getPrompt();
    }

    private boolean isOrderBookEvent(String eventType, CoindcxMarketType marketType) {
        if (marketType == CoindcxMarketType.FUTURES) {
            return eventType.equals(CoindcxWebSocketType.DepthSnapshot.getSerializedValue());
        }
        return eventType.equals(CoindcxWebSocketType.DepthUpdate.getSerializedValue())
                || eventType.equals(CoindcxWebSocketType.DepthUpdate20.getSerializedValue());
    }

    private <T> Observable<T> triggerObservableBody(Observable<T> observable) {
        Consumer<T> NOOP = whatever -> {};
        observable.subscribe(NOOP);
        return observable;
    }

    private <T> T readTransaction(JsonNode message, TypeReference<T> type, CoindcxWebSocketType transactionType) {
        try {
            return objectMapper.readValue(message.toString(), type);
        } catch (IOException e) {
            throw new ExchangeException(
                    String.format("Unable to parse %s transaction", transactionType), e);
        }
    }
}
