package org.knowm.xchange.coindcx;

import org.knowm.xchange.coindcx.dto.trade.*;
import si.mazi.rescu.ParamsDigest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CoindcxAuthenticated extends Coindcx {
    String API_KEY = "X-AUTH-APIKEY";
    String SIGNATURE = "X-AUTH-SIGNATURE";

    @POST
    @Path("exchange/v1/orders/create")
    Map<String, List<CoindcxOrder>> newOrder(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            CoindcxNewOrderRequest payload
    ) throws IOException;

    @POST
    @Path("exchange/v1/orders/cancel")
    void cancelOrder(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            CoindcxCancelOrderRequest payload
    ) throws IOException;

    @POST
    @Path("exchange/v1/orders/status")
    CoindcxOrder orderStatus(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            CoindcxOrderStatusRequest payload
    ) throws IOException;

    @POST
    @Path("exchange/v1/orders/edit")
    CoindcxOrder editPrice(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            CoindcxEditPriceRequest payload
    ) throws IOException;

    @POST
    @Path("exchange/v1/users/balances")
    List<CoindcxBalance> getBalances(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            Map<String, Object> payload
    ) throws IOException;

    @POST
    @Path("exchange/v1/orders/trade_history")
    List<Map<Object, Object>> getTradeHistory(
            @HeaderParam(API_KEY) String apiKey,
            @HeaderParam(SIGNATURE) ParamsDigest signature,
            CoindcxTradeHistoryRequest payload
    ) throws IOException;
}
