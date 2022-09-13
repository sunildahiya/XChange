package org.knowm.xchange.coindcx.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CoindcxOrderStatus {
    @JsonProperty("init")
    Init,
    @JsonProperty("open")
    Open,
    @JsonProperty("partial_entry")
    PartialEntry,
    @JsonProperty("filled")
    Filled,
    @JsonProperty("partial_close")
    PartialClose,
    @JsonProperty("partially_filled")
    PartiallyFilled,
    @JsonProperty("partially_cancelled")
    PartiallyCancelled,
    @JsonProperty("cancelled")
    Cancelled,
    @JsonProperty("rejected")
    Rejected,
    @JsonProperty("close")
    Close,
    @JsonProperty("triggered")
    Triggered;
}
