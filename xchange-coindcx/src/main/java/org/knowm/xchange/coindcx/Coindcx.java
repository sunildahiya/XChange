package org.knowm.xchange.coindcx;

import org.knowm.xchange.coindcx.dto.meta.MarketDetails;
import org.knowm.xchange.coindcx.dto.trade.CoindcxTrade;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public interface Coindcx {
    @GET
    @Path("exchange/v1/markets_details")
    List<MarketDetails> marketDetails();

    @GET
    @Path("market_data/trade_history")
    List<CoindcxTrade> tradeHistory();
}
