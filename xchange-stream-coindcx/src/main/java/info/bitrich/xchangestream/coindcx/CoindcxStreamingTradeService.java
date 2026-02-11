package info.bitrich.xchangestream.coindcx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.coindcx.dto.BaseCoindcxWebSocketTransaction.CoindcxWebSocketType;
import info.bitrich.xchangestream.coindcx.dto.FuturesOrderUpdateCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.coindcx.dto.OrderUpdateCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.coindcx.dto.UserTradeCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.coindcx.CoindcxAdapters;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;

import java.sql.Date;
import java.util.stream.Collectors;

@Slf4j
public class CoindcxStreamingTradeService implements StreamingTradeService {
    private final CoindcxStreamingService userDataStreamingService;
    private final Subject<UserTradeCoindcxWebSocketTransaction> tradePublisher = PublishSubject.<UserTradeCoindcxWebSocketTransaction>create().toSerialized();
    private final Subject<OrderUpdateCoindcxWebSocketTransaction> orderUpdatePublisher = PublishSubject.<OrderUpdateCoindcxWebSocketTransaction>create().toSerialized();
    private final Subject<FuturesOrderUpdateCoindcxWebSocketTransaction> futuresOrderUpdatePublisher = PublishSubject.<FuturesOrderUpdateCoindcxWebSocketTransaction>create().toSerialized();
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

    public CoindcxStreamingTradeService(CoindcxStreamingService userDataStreamingService) {
        this.userDataStreamingService = userDataStreamingService;
    }

    public void openSubscriptions() {
        if (userDataStreamingService != null) {
            Observable<JsonNode> userDataSubscriber = userDataStreamingService.subscribeChannel("coindcx");
            userDataSubscriber
                    .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.TradeUpdate.getSerializedValue()))
                    .map(message ->
                            (UserTradeCoindcxWebSocketTransaction) objectMapper.readerFor(new TypeReference<UserTradeCoindcxWebSocketTransaction>(){}).readValue(message)
                    ).subscribe(tradePublisher::onNext);
            userDataSubscriber
                    .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.OrderUpdate.getSerializedValue()))
                    .map(message ->
                            (OrderUpdateCoindcxWebSocketTransaction) objectMapper.readerFor(new TypeReference<OrderUpdateCoindcxWebSocketTransaction>(){}).readValue(message)
                    ).subscribe(orderUpdatePublisher::onNext);
            userDataSubscriber
                    .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.FuturesOrderUpdate.getSerializedValue()))
                    .map(message ->
                            (FuturesOrderUpdateCoindcxWebSocketTransaction) objectMapper.readerFor(new TypeReference<FuturesOrderUpdateCoindcxWebSocketTransaction>(){}).readValue(message)
                    ).subscribe(futuresOrderUpdatePublisher::onNext);
        }
    }

    @Override
    public Observable<UserTrade> getUserTrades() {
        return tradePublisher
                .map(tradeTransaction ->
                        tradeTransaction.getTrades()
                                .stream().map(t ->
                                        new UserTrade.Builder()
                                                .orderId(t.orderId)
                                                .price(t.price)
                                                .originalAmount(t.quantity)
                                                .instrument(CoindcxAdapters.adaptSymbol(t.symbol))
                                                .timestamp(new Date(t.tradeTime))
                                                .type(CoindcxAdapters.convertType(t.isBuyerMarketMaker))
                                                .feeAmount(t.fee)
                                                .feeCurrency(CoindcxAdapters.adaptSymbol(t.symbol).base)
                                                .build()
                                ).collect(Collectors.toList())
                ).flatMap(Observable::fromIterable);
    }

    @Override
    public Observable<Order> getOrderChanges() {
        Observable<Order> spotOrderChanges = orderUpdatePublisher
                .map(orderUpdateTransaction ->
                        orderUpdateTransaction
                                .getOrders()
                                .stream().map(CoindcxAdapters::adaptOrder).collect(Collectors.toList()))
                .flatMap(Observable::fromIterable);
        Observable<Order> futuresOrderChanges = futuresOrderUpdatePublisher
                .map(orderUpdateTransaction ->
                        orderUpdateTransaction
                                .getOrders()
                                .stream().map(CoindcxAdapters::adaptOrder).collect(Collectors.toList()))
                .flatMap(Observable::fromIterable);
        return Observable.merge(spotOrderChanges, futuresOrderChanges);
    }
}
