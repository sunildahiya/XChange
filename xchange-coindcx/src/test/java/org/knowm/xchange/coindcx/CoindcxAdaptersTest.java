package org.knowm.xchange.coindcx;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.Test;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesOrder;
import org.knowm.xchange.coindcx.dto.trade.CoindcxFuturesPosition;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.OpenPosition;

public class CoindcxAdaptersTest {

  @Test
  public void adaptSymbolSupportsFuturesFormat() {
    CurrencyPair pair = CoindcxAdapters.adaptSymbol("B-BTC_USDT@trades-futures");
    assertEquals(new CurrencyPair("BTC", "USDT"), pair);
  }

  @Test
  public void adaptFuturesOrderMapsToCoreOrder() {
    CoindcxFuturesOrder futuresOrder = new CoindcxFuturesOrder();
    futuresOrder.setId("order-1");
    futuresOrder.setClientOrderId("client-1");
    futuresOrder.setPair("B-BTC_USDT");
    futuresOrder.setSide("buy");
    futuresOrder.setOrderType("limit_order");
    futuresOrder.setStatus("partially_filled");
    futuresOrder.setTotalQuantity(new BigDecimal("10"));
    futuresOrder.setRemainingQuantity(new BigDecimal("2"));
    futuresOrder.setPricePerUnit(new BigDecimal("50000"));
    futuresOrder.setAvgPrice(new BigDecimal("49900"));
    futuresOrder.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));

    Order order = CoindcxAdapters.adaptOrder(futuresOrder);

    assertEquals(Order.OrderType.BID, order.getType());
    assertEquals(Order.OrderStatus.PARTIALLY_FILLED, order.getStatus());
    assertEquals(new CurrencyPair("BTC", "USDT"), order.getInstrument());
    assertEquals(new BigDecimal("8"), order.getCumulativeAmount());
  }

  @Test
  public void adaptFuturesPositionMapsToOpenPosition() {
    CoindcxFuturesPosition futuresPosition = new CoindcxFuturesPosition();
    futuresPosition.setPair("B-ETH_USDT");
    futuresPosition.setActivePos(new BigDecimal("-1.5"));
    futuresPosition.setAvgPrice(new BigDecimal("2500"));
    futuresPosition.setLiquidationPrice(new BigDecimal("2800"));

    OpenPosition openPosition = CoindcxAdapters.adaptOpenPosition(futuresPosition);

    assertEquals(new CurrencyPair("ETH", "USDT"), openPosition.getInstrument());
    assertEquals(OpenPosition.Type.SHORT, openPosition.getType());
    assertEquals(new BigDecimal("1.5"), openPosition.getSize());
  }
}
