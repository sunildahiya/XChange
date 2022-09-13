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
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CoindcxStreamingMarketDataService implements StreamingMarketDataService {
    private final Map<CurrencyPair, Observable<OrderBook>> orderbookSubscriptions;
    private final CoindcxStreamingService service;
    private final Map<CurrencyPair, Observable<DepthCoindcxWebSocketTransaction>> orderbookRawUpdatesSubscriptions;
    private final Map<CurrencyPair, Observable<TradeCoindcxWebSocketTransaction>> tradeSubscriptions;
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
        return orderbookSubscriptions.computeIfAbsent(currencyPair, this::initOrderbookIfAbsent);
    }

    @Override
    public Observable<OrderBook> getOrderbookChanges(CurrencyPair currencyPair, Object... args) {
        return orderbookSubscriptions.computeIfAbsent(currencyPair, this::initOrderbookIfAbsent);
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        return tradeSubscriptions.computeIfAbsent(currencyPair, this::initTradeSubscription)
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

    private Observable<OrderBook> initOrderbookIfAbsent(CurrencyPair currencyPair) {
        orderbookRawUpdatesSubscriptions.computeIfAbsent(currencyPair, ignore -> triggerObservableBody(rawOrderBookUpdates(currencyPair)));
        return orderbookRawUpdatesSubscriptions.get(currencyPair)
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
                currencyPair, triggerObservableBody(rawOrderBookUpdates(currencyPair)));
    }

    private Observable<TradeCoindcxWebSocketTransaction> initTradeSubscription(CurrencyPair currencyPair) {
        return triggerObservableBody(
                 service.subscribeChannel(channelFromCurrency(currencyPair))
                        .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.NewTrade.getSerializedValue()))
                        .map(it -> this.readTransaction(it, new TypeReference<TradeCoindcxWebSocketTransaction>() {}, CoindcxWebSocketType.NewTrade))
        );
    }

    private Observable<DepthCoindcxWebSocketTransaction> rawOrderBookUpdates(
            CurrencyPair currencyPair) {
        return service
                .subscribeChannel(channelFromCurrency(currencyPair))
                .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.DepthUpdate.getSerializedValue()))
                .map(
                        it ->
                                this.readTransaction(
                                        it, new TypeReference<DepthCoindcxWebSocketTransaction>(){}, CoindcxWebSocketType.DepthUpdate));
    }

    private String channelFromCurrency(CurrencyPair currencyPair) {
        // We are supporting only coindcx native markets
        String eCode = "I";
        if (!currencyPair.counter.getCurrencyCode().equals("INR"))
            eCode = "B";
        return String.format("%s-%s_%s", eCode, currencyPair.base, currencyPair.counter);
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
