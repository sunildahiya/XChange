package info.bitrich.xchangestream.coindcx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.coindcx.dto.BaseCoindcxWebSocketTransaction;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.coindcx.service.CoindcxHmacDigest;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CoindcxStreamingService extends JsonNettyStreamingService {
    protected final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();
    private final ExchangeSpecification exchangeSpec;
    private CoindcxHmacDigest messageSigner;
    private final Disposable keepAlive;
    private static final String USER_CHANNEL = "coindcx";
    public CoindcxStreamingService(String apiUrl, ExchangeSpecification exchangeSpec) {
        super(apiUrl);
        this.exchangeSpec = exchangeSpec;
        this.messageSigner = new CoindcxHmacDigest(exchangeSpec.getSecretKey());
        // Send a keepalive every 25 seconds
        this.keepAlive = Observable.interval(25, TimeUnit.SECONDS).subscribe(x -> keepAlive());
    }

    private void keepAlive() {
        log.debug("Sending ping message");
        super.sendMessage("2");
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        return message.get("channel").asText();
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        if (channelName.equals(USER_CHANNEL)) {
            if (messageSigner == null) {
                throw new ExchangeException("Please provide message signer");
            }
            String signature = messageSigner.signMessage("{\"channel\":\"coindcx\"}");
            return String.format(
                    "42[\"join\", {\"channelName\": \"%s\", \"authSignature\": \"%s\", \"apiKey\": \"%s\"}]",
                    channelName, signature, exchangeSpec.getApiKey()
            );
        }
        return String.format("42[\"join\", {\"channelName\": \"%s\"}]", channelName);
    }

    @Override
    public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
        return String.format("42[\"leave\", {\"channelName\": \"%s\"}]", channelName);
    }

    @Override
    public void messageHandler(String message) {
        // Ignore socket.io specific messages
        // 2 - ping, 3 - pong
        if (message.startsWith("0") || message.startsWith("40") || message.startsWith("3") || message.length() == 0) {
            log.debug("Message ignored: " + message + " " + message.length());
            return;
        }

        JsonNode jsonNode = getJsonMessage(message);
        if (jsonNode != null) {
            String type = jsonNode.get("type").asText();
            if (BaseCoindcxWebSocketTransaction.CoindcxWebSocketType.fromTransactionValue(type) == null) {
                if (!type.equals("depth-update"))
                    log.error("Received unrecognised transaction - " + type);
                return;
            }
            handleMessage(jsonNode);
        }
    }

    private JsonNode getJsonMessage(String message) {
        try {
            JsonNode dataNode = objectMapper.readTree(message.substring(2));
            String eventType = dataNode.get(0).asText();
            if (eventType.equals("depth-update") || eventType.equals("depth-update-20"))
                log.debug("Received depth message - {}", message);
            else if (eventType.equals("new-trade"))
                log.debug("Received market trade message - {}", message);
            else
                log.info("Received message - {}", message);
            if (BaseCoindcxWebSocketTransaction.CoindcxWebSocketType.isAuthenticatedEvent(eventType)) {
                Map<String, Object> normalizedMessageMap = new HashMap<>();
                normalizedMessageMap.put("type", eventType);
                normalizedMessageMap.put("E", System.currentTimeMillis());
                normalizedMessageMap.put("channel", "coindcx");
                normalizedMessageMap.put("data", objectMapper.readTree(dataNode.get(1).get("data").asText()));
                return objectMapper.valueToTree(normalizedMessageMap);
            }
            return objectMapper.readTree(dataNode.get(1).get("data").asText());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
