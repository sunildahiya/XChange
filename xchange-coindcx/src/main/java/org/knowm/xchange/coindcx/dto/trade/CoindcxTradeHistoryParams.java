package org.knowm.xchange.coindcx.dto.trade;

import org.knowm.xchange.service.trade.params.TradeHistoryParamLimit;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsIdSpan;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsTimeSpan;

import java.util.Date;

public class CoindcxTradeHistoryParams implements
        TradeHistoryParamLimit, TradeHistoryParamsIdSpan, TradeHistoryParamsTimeSpan
{
    private Integer limit;
    private String startId;
    private Date startTime;
    private Date endTime;

    public CoindcxTradeHistoryParams() {}

    public CoindcxTradeHistoryParams(String startId) {
        this.startId = startId;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String getStartId() {
        return startId;
    }

    @Override
    public void setStartId(String startId) {
        this.startId = startId;
    }

    /**
     * @return null as endId is not supported on Coindcx
     */
    @Override
    public String getEndId() {
        return null;
    }

    /** endId is not supported on Coindcx */
    @Override
    public void setEndId(String endId) {
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}
