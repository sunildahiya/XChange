package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CoindcxTradeHistoryRequest {
    private Integer limit;

    @JsonProperty("from_id")
    private Long fromId;

    private SortType sort;
    private final Long timestamp;

    @JsonProperty("from_timestamp")
    private Long fromTimestamp;

    @JsonProperty("to_timestamp")
    private Long toTimestamp;

    private String symbol;

    public enum SortType {
        @JsonProperty("asc")
        ASC,
        @JsonProperty("desc")
        DESC
    }

    public CoindcxTradeHistoryRequest(long timestamp) {
        this.timestamp = timestamp;
    }

    public CoindcxTradeHistoryRequest(long fromId, long timestamp) {
        this(timestamp);
        this.fromId = fromId;
    }

    public CoindcxTradeHistoryRequest(long fromTime, long toTime, long timestamp) {
        this(timestamp);
        this.fromTimestamp = fromTime;
        this.toTimestamp = toTime;
    }
}
