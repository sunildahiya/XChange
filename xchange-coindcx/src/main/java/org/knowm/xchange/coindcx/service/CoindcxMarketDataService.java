package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;

public class CoindcxMarketDataService extends CoindcxMarketDataServiceRaw implements MarketDataService {
    protected CoindcxMarketDataService(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    @Override
    public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
        return MarketDataService.super.getTrades(currencyPair, args);
    }


}
