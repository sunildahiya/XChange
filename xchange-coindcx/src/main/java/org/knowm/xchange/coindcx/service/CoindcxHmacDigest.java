package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;

import static org.knowm.xchange.utils.DigestUtils.bytesToHex;

public class CoindcxHmacDigest extends BaseParamsDigest {

    public CoindcxHmacDigest(String secretKey) throws IllegalArgumentException {
        super(secretKey, HMAC_SHA_256);
    }

    public static CoindcxHmacDigest createInstance(String secretKey) {
        return secretKey == null ? null : new CoindcxHmacDigest(secretKey);
    }

    @Override
    public String digestParams(RestInvocation restInvocation) {
        final String input;
        switch (restInvocation.getHttpMethod()) {
            case "POST":
                input = restInvocation.getRequestBody();
                break;
            default:
                throw new RuntimeException("Not supported http method: " + restInvocation.getHttpMethod());
        }
        return signMessage(input);
    }

    public String signMessage(String message) {
        Mac mac = getMac();
        mac.update(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(mac.doFinal());
    }
}
