package org.knowm.xchange.coindcx.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public final class MarketDetails {
    @JsonProperty("coindcx_name")
    private String coindcxName;

    @JsonProperty("base_currency_short_name")
    private String baseCurrencyShortName;

    @JsonProperty("target_currency_short_name")
    private String targetCurrencyShortName;

    @JsonProperty("target_currency_name")
    private String targetCurrencyName;

    @JsonProperty("base_currency_name")
    private String baseCurrencyName;

    @JsonProperty("min_quantity")
    private BigDecimal minQuantity;

    @JsonProperty("max_quantity")
    private BigDecimal maxQuantity;

    @JsonProperty("min_price")
    private BigDecimal minPrice;

    @JsonProperty("max_price")
    private BigDecimal maxPrice;

    @JsonProperty("min_notional")
    private BigDecimal minNotional;

    @JsonProperty("base_currency_precision")
    private int baseCurrencyPrecision;

    @JsonProperty("target_currency_precision")
    private int targetCurrencyPrecision;

    private BigDecimal step;

    @JsonProperty("order_type")
    private List<String> orderTypes;

    private String symbol;

    private String ecode;

    @JsonProperty("max_leverage")
    private int maxLeverage;

    @JsonProperty("max_leverage_short")
    private Object maxLeverageShort;

    private String pair;

    private String status;

    public String getCoindcxName() {
        return coindcxName;
    }

    public void setCoindcxName(String coindcxName) {
        this.coindcxName = coindcxName;
    }

    public String getBaseCurrencyShortName() {
        return baseCurrencyShortName;
    }

    public void setBaseCurrencyShortName(String baseCurrencyShortName) {
        this.baseCurrencyShortName = baseCurrencyShortName;
    }

    public String getTargetCurrencyShortName() {
        return targetCurrencyShortName;
    }

    public void setTargetCurrencyShortName(String targetCurrencyShortName) {
        this.targetCurrencyShortName = targetCurrencyShortName;
    }

    public String getTargetCurrencyName() {
        return targetCurrencyName;
    }

    public void setTargetCurrencyName(String targetCurrencyName) {
        this.targetCurrencyName = targetCurrencyName;
    }

    public String getBaseCurrencyName() {
        return baseCurrencyName;
    }

    public void setBaseCurrencyName(String baseCurrencyName) {
        this.baseCurrencyName = baseCurrencyName;
    }

    public BigDecimal getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(BigDecimal minQuantity) {
        this.minQuantity = minQuantity;
    }

    public BigDecimal getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(BigDecimal maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinNotional() {
        return minNotional;
    }

    public void setMinNotional(BigDecimal minNotional) {
        this.minNotional = minNotional;
    }

    public int getBaseCurrencyPrecision() {
        return baseCurrencyPrecision;
    }

    public void setBaseCurrencyPrecision(int baseCurrencyPrecision) {
        this.baseCurrencyPrecision = baseCurrencyPrecision;
    }

    public int getTargetCurrencyPrecision() {
        return targetCurrencyPrecision;
    }

    public void setTargetCurrencyPrecision(int targetCurrencyPrecision) {
        this.targetCurrencyPrecision = targetCurrencyPrecision;
    }

    public BigDecimal getStep() {
        return step;
    }

    public void setStep(BigDecimal step) {
        this.step = step;
    }

    public List<String> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(List<String> orderTypes) {
        this.orderTypes = orderTypes;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getEcode() {
        return ecode;
    }

    public void setEcode(String ecode) {
        this.ecode = ecode;
    }

    public int getMaxLeverage() {
        return maxLeverage;
    }

    public void setMaxLeverage(int maxLeverage) {
        this.maxLeverage = maxLeverage;
    }

    public Object getMaxLeverageShort() {
        return maxLeverageShort;
    }

    public void setMaxLeverageShort(Object maxLeverageShort) {
        this.maxLeverageShort = maxLeverageShort;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
