package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

public class CoindcxBaseService extends BaseExchangeService<CoindcxExchange> implements BaseService {
    protected final CoindcxAuthenticated coindcx;
    protected final String apiKey;
    protected final ParamsDigest signatureCreator;

    protected CoindcxBaseService(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange);
        this.coindcx = coindcx;
        this.apiKey = exchange.getExchangeSpecification().getApiKey();
        this.signatureCreator =
                CoindcxHmacDigest.createInstance(exchange.getExchangeSpecification().getSecretKey());
    }

    public SynchronizedValueFactory<Long> getTimestampFactory() {
        return exchange.getTimestampFactory();
    }
}
