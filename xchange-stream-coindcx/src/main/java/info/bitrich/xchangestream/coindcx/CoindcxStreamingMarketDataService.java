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
    private final Map<Instrument, Observable<OrderBook>> orderbookSubscriptions; // Raw diff converted to OrderBook
    private final CoindcxStreamingService service;
    private final Map<Instrument, Observable<DepthCoindcxWebSocketTransaction>> orderbookRawUpdatesSubscriptions; // Raw diff update from Coindcx
    private final Map<Instrument, Observable<TradeCoindcxWebSocketTransaction>> tradeSubscriptions;
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
    public Observable<OrderBook> getOrderbookChanges(Instrument instrument, Object... args) {
        if (!(instrument instanceof CurrencyPair || instrument instanceof FuturesContract)) {
            throw new IllegalArgumentException("Instrument must be a CurrencyPair or FuturesContract");
        }
        return orderbookSubscriptions.computeIfAbsent(instrument, ignore -> initOrderbookIfAbsent(instrument));
    }

    @Override
    public Observable<Trade> getTrades(Instrument instrument, Object... args) {
        return tradeSubscriptions.computeIfAbsent(instrument, ignore -> initTradeSubscription(instrument))
                .map(rawTrade ->
                    new Trade.Builder()
                            .type(CoindcxAdapters.convertType(rawTrade.isBuyerMarketMaker()))
                            .originalAmount(rawTrade.getQuantity())
                            .instrument(instrument)
                            .price(rawTrade.getPrice())
                            .timestamp(new Date(rawTrade.getEventTime()))
                            .build()
                );
    }

    private Observable<OrderBook> initOrderbookIfAbsent(Instrument instrument) {
        orderbookRawUpdatesSubscriptions.computeIfAbsent(instrument, ignore -> triggerObservableBody(rawOrderBookUpdates(instrument)));
        return orderbookRawUpdatesSubscriptions.get(instrument)
                .map(transaction -> {
                    CoindcxOrderbook coindcxOrderbook = transaction.getOrderbook();
                    List<LimitOrder> bids =
                            coindcxOrderbook.bids.entrySet().stream().map( priceAndQty
                                    -> new LimitOrder(Order.OrderType.BID, priceAndQty.getValue(), instrument, null, null, priceAndQty.getKey())
                            ).collect(Collectors.toList());

                    List<LimitOrder> asks =
                            coindcxOrderbook.asks.entrySet().stream().map( priceAndQty
                                    -> new LimitOrder(Order.OrderType.ASK, priceAndQty.getValue(), instrument, null, null, priceAndQty.getKey())
                            ).collect(Collectors.toList());
                    return new OrderBook(instrument, null, asks, bids);
                });
    }

    private void initRawOrderBookUpdatesSubscription(Instrument instrument) {
        orderbookRawUpdatesSubscriptions.put(instrument, triggerObservableBody(rawOrderBookUpdates(instrument)));
    }

    private Observable<TradeCoindcxWebSocketTransaction> initTradeSubscription(Instrument instrument) {
        String channel = getCoindcxInstrumentName(instrument) + "@" + ((instrument instanceof CurrencyPair) ? "trades" : "trades-futures");
        return triggerObservableBody(
                 service.subscribeChannel(channel)
                        .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.NewTrade.getSerializedValue()))
                        .map(it -> this.readTransaction(it, new TypeReference<TradeCoindcxWebSocketTransaction>() {}, CoindcxWebSocketType.NewTrade))
        );
    }

    private Observable<DepthCoindcxWebSocketTransaction> rawOrderBookUpdates(Instrument instrument) {
        String channel = getCoindcxInstrumentName(instrument) + (instrument instanceof CurrencyPair ? "@orderbook@20" : "@orderbook@20-futures");
        return service
                .subscribeChannel(channel)
                .filter(message -> isOrderBookEvent(message.get("type").asText()))
                .map(
                        it ->
                                this.readTransaction(
                                        it, new TypeReference<DepthCoindcxWebSocketTransaction>(){}, CoindcxWebSocketType.DepthSnapshot));
    }

    private String getCoindcxInstrumentName(Instrument instrument) {
        if (instrument instanceof CurrencyPair) {
            CurrencyPair currencyPair = (CurrencyPair) instrument;
            // We are supporting Coindcx and Kucoin markets
            String eCode = "I";
            if (!currencyPair.counter.getCurrencyCode().equals("INR"))
                eCode = "KC";
            return String.format("%s-%s_%s", eCode, currencyPair.base, currencyPair.counter);
        } else if (instrument instanceof FuturesContract) {
            FuturesContract futuresContract = (FuturesContract) instrument;
            return String.format("B-%s_%s", futuresContract.getCurrencyPair().base, futuresContract.getCurrencyPair().counter);
        }
        throw new IllegalArgumentException("Instrument must be a CurrencyPair or FuturesContract");
    }

    private CoindcxMarketType getMarketType(Object... args) {
        if (args != null && args.length > 0 && args[0] instanceof CoindcxMarketType) {
            return (CoindcxMarketType) args[0];
        }
        return CoindcxMarketType.SPOT;
    }

    private boolean isOrderBookEvent(String eventType) {
        return eventType.equals(CoindcxWebSocketType.DepthUpdate.getSerializedValue())
                || eventType.equals(CoindcxWebSocketType.DepthUpdate20.getSerializedValue())
                || eventType.equals(CoindcxWebSocketType.DepthSnapshot.getSerializedValue());
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
