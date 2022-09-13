package org.knowm.xchange.coindcx;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.coindcx.dto.meta.MarketDetails;
import org.knowm.xchange.coindcx.service.CoindcxAccountService;
import org.knowm.xchange.coindcx.service.CoindcxTradeService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.Instrument;
import si.mazi.rescu.ClientConfig;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoindcxExchange extends BaseExchange {

    protected CoindcxAuthenticated coindcx;
    protected SynchronizedValueFactory<Long> timestampFactory;

    @Override
    protected void initServices() {
        this.coindcx =
                ExchangeRestProxyBuilder
                        .forInterface(CoindcxAuthenticated.class, getExchangeSpecification())
                        .clientConfig(clientConfig())
                        .build();
        this.timestampFactory = new CoindcxTimestampFactory();
        this.tradeService = new CoindcxTradeService(this, coindcx);
        this.accountService = new CoindcxAccountService(this, coindcx);
    }

    private ClientConfig clientConfig() {
        ClientConfig cfg = new ClientConfig();
        cfg.setJacksonObjectMapperFactory(new CustomJacksonObjectMapperFactory());
        return cfg;
    }

    @Override
    public List<Instrument> getExchangeInstruments() {
        return super.getExchangeInstruments();
    }

    @Override
    public ResilienceRegistries getResilienceRegistries() {
        return super.getResilienceRegistries();
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification specification = new ExchangeSpecification(this.getClass());
//        specification.setExchangeName("Coindcx");
        specification.setExchangeDescription("Coindcx Exchange");
        specification.setSslUri("https://api.coindcx.com");
        specification.setHost("www.coindcx.com");
        specification.setPort(80);
        return specification;
    }

    @Override
    public void remoteInit() throws IOException, ExchangeException {
        List<MarketDetails> marketDetailsList = coindcx.marketDetails();

        Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = new HashMap<>();
        marketDetailsList.forEach(marketDetails -> {
            CurrencyPairMetaData metaData = new CurrencyPairMetaData.Builder()
                    .minimumAmount(marketDetails.getMinQuantity())
                    .maximumAmount(marketDetails.getMaxQuantity())
                    .counterMinimumAmount(marketDetails.getMinNotional())
                    .baseScale(marketDetails.getTargetCurrencyPrecision())
                    .priceScale(marketDetails.getBaseCurrencyPrecision())
                    .amountStepSize(marketDetails.getStep())
                    .build();
            currencyPairs.put(
                    CoindcxAdapters.adaptSymbol(marketDetails.getSymbol()),
                    metaData
            );
        });
        exchangeMetaData = new ExchangeMetaData(currencyPairs, null, null, null, null);
    }

    public SynchronizedValueFactory<Long> getTimestampFactory() {
        return timestampFactory;
    }
}
