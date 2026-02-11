package info.bitrich.xchangestream.coindcx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.coindcx.dto.BalanceCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.coindcx.dto.BaseCoindcxWebSocketTransaction.CoindcxWebSocketType;
import info.bitrich.xchangestream.coindcx.dto.PositionUpdateCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.core.StreamingAccountService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.coindcx.CoindcxAdapters;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.OpenPosition;

import java.util.stream.Collectors;

@Slf4j
public class CoindcxStreamingAccountService implements StreamingAccountService {
    private final CoindcxStreamingService userDataStreamingService;
    private final Subject<BalanceCoindcxWebSocketTransaction> balancePublisher = PublishSubject.<BalanceCoindcxWebSocketTransaction>create().toSerialized();
    private final Subject<PositionUpdateCoindcxWebSocketTransaction> positionPublisher = PublishSubject.<PositionUpdateCoindcxWebSocketTransaction>create().toSerialized();
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();
    private volatile boolean subscriptionsInitialized = false;

    public CoindcxStreamingAccountService(CoindcxStreamingService userDataStreamingService) {
        this.userDataStreamingService = userDataStreamingService;
    }

    public void openSubscriptions() {
        if (subscriptionsInitialized || userDataStreamingService == null) {
            return;
        }
        synchronized (this) {
            if (subscriptionsInitialized) {
                return;
            }
            userDataStreamingService
                    .subscribeChannel("coindcx")
                    .filter(message -> {
                        log.debug("Message received in account service");
                        return message.get("type").asText().equals(CoindcxWebSocketType.BalanceUpdate.getSerializedValue());
                    })
                    .map(message ->
                            (BalanceCoindcxWebSocketTransaction) objectMapper.readerFor(new TypeReference<BalanceCoindcxWebSocketTransaction>(){}).readValue(message)
                    ).subscribe(balancePublisher::onNext);
            userDataStreamingService
                    .subscribeChannel("coindcx")
                    .filter(message -> message.get("type").asText().equals(CoindcxWebSocketType.PositionUpdate.getSerializedValue()))
                    .map(message ->
                            (PositionUpdateCoindcxWebSocketTransaction) objectMapper.readerFor(new TypeReference<PositionUpdateCoindcxWebSocketTransaction>(){}).readValue(message)
                    ).subscribe(positionPublisher::onNext);
            subscriptionsInitialized = true;
        }
    }

    @Override
    public Observable<Balance> getBalanceChanges() {
        openSubscriptions();
        return balancePublisher
                .map(balanceTransaction ->
                        balanceTransaction.getCurrencyBalances()
                                .stream().map(b ->
                                        new Balance.Builder()
                                                .currency(new Currency(b.currency))
                                                .available(b.balance)
                                                .frozen(b.lockedBalance)
                                                .total(b.balance.add(b.lockedBalance))
                                                .build()
                                ).collect(Collectors.toList())
                ).flatMap(Observable::fromIterable);
    }

    public Observable<OpenPosition> getPositionChanges() {
        openSubscriptions();
        return positionPublisher
                .map(positionTransaction ->
                        positionTransaction.getPositions()
                                .stream()
                                .map(CoindcxAdapters::adaptOpenPosition)
                                .collect(Collectors.toList()))
                .flatMap(Observable::fromIterable);
    }
}
