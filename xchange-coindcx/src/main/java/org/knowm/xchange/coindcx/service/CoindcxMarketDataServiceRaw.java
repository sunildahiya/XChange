package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.coindcx.dto.trade.CoindcxTrade;

import java.util.List;

public class CoindcxMarketDataServiceRaw extends CoindcxBaseService {

    protected CoindcxMarketDataServiceRaw(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    public List<CoindcxTrade> tradeHistory() {
        return coindcx.tradeHistory();
    }
}
