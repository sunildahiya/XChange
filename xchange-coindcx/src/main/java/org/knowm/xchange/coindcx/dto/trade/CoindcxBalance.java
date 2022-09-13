package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class CoindcxBalance {
    public String currency;
    public BigDecimal balance;
    public BigDecimal lockedBalance;

    public CoindcxBalance(@JsonProperty("currency") String currency,
                          @JsonProperty("balance") BigDecimal balance,
                          @JsonProperty("locked_balance") BigDecimal lockedBalance)
    {
        this.currency = currency;
        this.balance = balance;
        this.lockedBalance = lockedBalance;
    }
}
