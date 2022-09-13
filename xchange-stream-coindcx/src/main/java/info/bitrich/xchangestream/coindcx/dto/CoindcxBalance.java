package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public class CoindcxBalance {
    public final String currency;
    public final BigDecimal balance;
    public final BigDecimal lockedBalance;

    public CoindcxBalance(
            @JsonProperty("currency") Map<String, Object> currencyDetails,
            @JsonProperty("balance") BigDecimal balance,
            @JsonProperty("locked_balance") BigDecimal lockedBalance
    ) {
        this.currency = (String) currencyDetails.get("short_name");
        this.balance = balance;
        this.lockedBalance = lockedBalance;
    }
}
