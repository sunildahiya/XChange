package info.bitrich.xchangestream.coindcx;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.junit.Test;
import org.knowm.xchange.ExchangeSpecification;

public class TestStreamingExchange {
    @Test
    public void test() throws InterruptedException {
        ExchangeSpecification spec = new ExchangeSpecification(CoindcxStreamingExchange.class);
//        spec.setExchangeName("Coindcx");
//        spec.setExchangeDescription("Coindcx Stream Api client");
        spec.setApiKey("f414af83daea0b979578af096b21e11090ce79cfe08fbe5b");
        spec.setSecretKey("5463efd85e423dbf1aa0039f7e4c284eb033868814da13ae82c0d5bf39bdf31e");

        CoindcxStreamingExchange streamingExchange = (CoindcxStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);
        streamingExchange.connect().doOnComplete(() -> {System.out.println("Successfully connected");}).blockingAwait();
//        streamingExchange.getStreamingMarketDataService().getOrderBook(new CurrencyPair("BTC", "INR"))
//                        .subscribe(System.out::println);
        streamingExchange.getStreamingTradeService()
                .getUserTrades()
                .subscribe(trade -> System.out.println("Received trade: " + trade));
        streamingExchange.getStreamingAccountService()
                .getBalanceChanges()
                        .subscribe(balance -> System.out.println("Received balance - " + balance));
        Thread.sleep(1000000);
    }
}
