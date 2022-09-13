package org.knowm.xchange.coindcx.service;

import com.google.common.collect.ImmutableMap;
import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.dto.trade.CoindcxBalance;

import java.io.IOException;
import java.util.List;

public class CoindcxAccountServiceRaw extends CoindcxBaseService {
    protected CoindcxAccountServiceRaw(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    public List<CoindcxBalance> getBalances() throws IOException {
        return coindcx.getBalances(
                apiKey,
                signatureCreator,
                ImmutableMap.of("timestamp", System.currentTimeMillis())
        );
    }
}
