package info.bitrich.xchangestream.coindcx;

import info.bitrich.xchangestream.core.*;
import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.coindcx.CoindcxExchange;

@Slf4j
public class CoindcxStreamingExchange extends CoindcxExchange implements StreamingExchange {

    private CoindcxStreamingService marketDataStreamingService;
    private CoindcxStreamingService tradeDataStreamingService;
    private CoindcxStreamingService accountStreamingService;
    private CoindcxStreamingMarketDataService streamingMarketDataService;
    private CoindcxStreamingTradeService streamingTradeService;
    private CoindcxStreamingAccountService streamingAccountService;

    @Override
    protected void initServices() {
        super.initServices();
        marketDataStreamingService = createStreamingService();
        tradeDataStreamingService = createStreamingService();
        accountStreamingService = createStreamingService();
        streamingMarketDataService = new CoindcxStreamingMarketDataService(marketDataStreamingService);
        streamingTradeService = new CoindcxStreamingTradeService(tradeDataStreamingService);
        streamingAccountService = new CoindcxStreamingAccountService(accountStreamingService);
    }

    @Override
    public Completable connect(ProductSubscription... args) {
        return Completable.mergeArray(
                marketDataStreamingService.connect().doOnComplete(() -> log.info("Coindcx marketDataStreamingService is successfully connected")),
                tradeDataStreamingService.connect().doOnComplete(() ->  log.info("Coindcx tradeDataStreamingService is successfully connected")),
                accountStreamingService.connect().doOnComplete(() ->  log.info("Coindcx accountStreamingService is successfully connected"))
        ).doOnComplete(() -> streamingTradeService.openSubscriptions());
    }

    @Override
    public Completable disconnect() {
        return null;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {

    }

    protected CoindcxStreamingService createStreamingService() {
        String path = "wss://stream.coindcx.com/socket.io/?EIO=3&transport=websocket";
        return new CoindcxStreamingService(path, exchangeSpecification);
    }

    @Override
    public StreamingMarketDataService getStreamingMarketDataService() {
        return streamingMarketDataService;
    }

    @Override
    public StreamingTradeService getStreamingTradeService() {
        return streamingTradeService;
    }

    @Override
    public StreamingAccountService getStreamingAccountService() {
        return streamingAccountService;
    }
}
