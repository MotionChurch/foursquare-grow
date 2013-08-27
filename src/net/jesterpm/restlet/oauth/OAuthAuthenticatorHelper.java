/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.restlet.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.Collections;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.header.ChallengeWriter;
import org.restlet.engine.header.Header;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.util.Base64;
import org.restlet.util.Series;

/**
 * Authentication helper for signing OAuth Requests.
 *
 * This implementation is limited to one consumer token/secret per restlet
 * engine. In practice this means you will only be able to interact with one
 * service provider unless you loaded/unloaded the AuthenticationHelper for
 * each request.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class OAuthAuthenticatorHelper extends AuthenticatorHelper {
    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String JAVA_SIGNATURE_METHOD = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    private final Random mRandom;
    private final Token mConsumerToken;

    /**
     * Package-private constructor.
     *
     * This class should only be instantiated by OAuthHelper.
     */
    OAuthAuthenticatorHelper(Token consumerToken) {
        super(ChallengeScheme.HTTP_OAUTH, true, false);

        mRandom = new Random();
        mConsumerToken = consumerToken;
    }

    @Override
    public void formatRequest(ChallengeWriter cw, ChallengeRequest cr,
            Response response, Series<Header> httpHeaders) throws IOException {

        throw new UnsupportedOperationException("OAuth Requests are not implemented");
    }

    @Override
    public void formatResponse(ChallengeWriter cw, ChallengeResponse response,
            Request request, Series<Header> httpHeaders) {

        try {
            Series<Parameter> authParams = new Series<Parameter>(Parameter.class);

            String nonce = String.valueOf(mRandom.nextInt());
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            authParams.add(new Parameter("oauth_consumer_key", mConsumerToken.getToken()));
            authParams.add(new Parameter("oauth_nonce", nonce));
            authParams.add(new Parameter("oauth_signature_method", SIGNATURE_METHOD));
            authParams.add(new Parameter("oauth_timestamp", timestamp));
            authParams.add(new Parameter("oauth_version", "1.0"));

            String accessToken = response.getIdentifier();
            if (accessToken != null) {
                authParams.add(new Parameter("oauth_token", accessToken));
            }

            // Generate Signature
            String signature = generateSignature(response, request, authParams);
            authParams.add(new Parameter("oauth_signature", signature));

            // Write Header
            for (Parameter p : authParams) {
                cw.appendQuotedChallengeParameter(encode(p.getName()), encode(p.getValue()));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to generate an OAuth Signature.
     */
    private String generateSignature(ChallengeResponse response, Request request,
           Series<Parameter> authParams)
        throws NoSuchAlgorithmException, InvalidKeyException, IOException,
                          UnsupportedEncodingException {

        // HTTP Request Method
        String httpMethod = request.getMethod().getName();

        // Request Url
        Reference url = request.getResourceRef();
        String requestUrl = encode(url.getScheme() + ":" + url.getHierarchicalPart());

        // Normalized parameters
        Series<Parameter> params = new Series<Parameter>(Parameter.class);

        // OAUTH Params
        params.addAll(authParams);

        // Query Params
        Form query = url.getQueryAsForm();
        params.addAll(query);

        // Sort it
        Collections.sort(params);

        StringBuilder normalizedParamsBuilder = new StringBuilder();
        for (Parameter p : params) {
            normalizedParamsBuilder.append('&');
            normalizedParamsBuilder.append(p.encode(CharacterSet.UTF_8));
        }
        String normalizedParams = encode(normalizedParamsBuilder.substring(1)); // remove the first &

        // Generate signature base
        String sigBase = httpMethod + "&" + requestUrl + "&" + normalizedParams.toString();

        // Sign the signature base
        Mac mac = Mac.getInstance(JAVA_SIGNATURE_METHOD);

        String accessTokenSecret = "";
        if (response.getIdentifier() != null) {
            accessTokenSecret = new String(response.getSecret());
        }

        byte[] keyBytes = (encode(mConsumerToken.getSecret()) + "&" + encode(accessTokenSecret)).getBytes(ENCODING);
        SecretKey key = new SecretKeySpec(keyBytes, JAVA_SIGNATURE_METHOD);
        mac.init(key);

        byte[] signature = mac.doFinal(sigBase.getBytes(ENCODING));

        return Base64.encode(signature, false).trim();
    }

    /**
     * Helper method to URL Encode Strings.
     */
    private String encode(String input) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, ENCODING);
    }
}
