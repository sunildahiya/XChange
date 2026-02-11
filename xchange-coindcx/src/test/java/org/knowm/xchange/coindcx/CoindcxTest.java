package org.knowm.xchange.coindcx;

import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.coindcx.service.CoindcxTradeServiceRaw;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.TradeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class CoindcxTest {
    @Test
    public void testNewOrder() throws IOException {
        Exchange coindcx = ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(CoindcxExchange.class);
        ExchangeSpecification specification = coindcx.getDefaultExchangeSpecification();
        specification.setApiKey("f414af83daea0b979578af096b21e11090ce79cfe08fbe5b");
        specification.setSecretKey("5463efd85e423dbf1aa0039f7e4c284eb033868814da13ae82c0d5bf39bdf31e");
        coindcx.applySpecification(specification);

//        Map balances = coindcx.getAccountService().getAccountInfo().getWallet().getBalances();

        CoindcxTradeServiceRaw tradeServiceRaw = (CoindcxTradeServiceRaw)  coindcx.getTradeService();
        tradeServiceRaw.getTradeHistory();
//        TradeService tradeService = coindcx.getTradeService();
//        LimitOrder limitOrder = new LimitOrder.Builder(Order.OrderType.BID, new CurrencyPair("BTC", "INR"))
//                .originalAmount(new BigDecimal("0.00007"))
//                .limitPrice(new BigDecimal("1600000.0"))
//                .build();
//        tradeService.placeLimitOrder(limitOrder);
    }
}
