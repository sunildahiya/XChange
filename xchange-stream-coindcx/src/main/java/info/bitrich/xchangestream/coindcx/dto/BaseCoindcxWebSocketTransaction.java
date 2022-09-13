package info.bitrich.xchangestream.coindcx.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseCoindcxWebSocketTransaction {
    public enum CoindcxWebSocketType {
        DepthUpdate("depth-update-20"),
        NewTrade("new-trade"),
        BalanceUpdate("balance-update"),
        TradeUpdate("trade-update"),
        OrderUpdate("order-update");

        private final String serializedValue;

        CoindcxWebSocketType(String serializedValue) {
            this.serializedValue = serializedValue;
        }

        public String getSerializedValue() {
            return serializedValue;
        }

        public static CoindcxWebSocketType fromTransactionValue(String value) {
            for (CoindcxWebSocketType type: CoindcxWebSocketType.values()) {
                if (type.serializedValue.equals(value))
                    return type;
            }
            return null;
        }

        public static boolean isAuthenticatedEvent(String eventType) {
            return eventType.equals(BalanceUpdate.serializedValue)
                    || eventType.equals(TradeUpdate.serializedValue)
                    || eventType.equals(OrderUpdate.serializedValue);
        }
    }

    protected final CoindcxWebSocketType eventType;
    protected final long eventTime;
    protected final String channel;

    public BaseCoindcxWebSocketTransaction(@JsonProperty("type") String eventType, @JsonProperty("E") long eventTime, @JsonProperty("channel") String channel) {
        this.eventType = CoindcxWebSocketType.fromTransactionValue(eventType);
        this.eventTime = eventTime;
        this.channel = channel;
    }

    public CoindcxWebSocketType getEventType() {
        return eventType;
    }

    public String getChannel() {
        return channel;
    }

    public long getEventTime() {
        return eventTime;
    }
}
