package com.scm.reader.livescanner.search;

import android.content.pm.PackageInfo;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.util.SignatureBuilder;

import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by franco on 17/06/16.
 */
public class VuforiaSearchBuilderRequest {

    private static final String QUERY_PARAM_MAX_NUM_KEY = "max_num_results";
    private static final String QUERY_PARAM_MAX_NUM_VAL = "10";
    private static final String QUERY_PARAM_INCLUDE_TARGET_DATA_KEY = "include_target_data";
    private static final String QUERY_PARAM_INCLUDE_TARGET_DATA_VAL = "all";

    private String API_KEY;
    private String API_SECRET;
    private static final String VUFORIA_QUERY_ENDPOINT = "https://cloudreco.vuforia.com/v1/query";

    private SearchRequestData mData;
    private int mResponseStatus = -1;
    private String mResponseBody = null;

    public VuforiaSearchBuilderRequest(SearchRequestData data, PackageInfo pInfo, String apiKey, String apiSecret) {
        mData = data;
        API_KEY = apiKey;
        API_SECRET = apiSecret;
    }

    public String query() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(QUERY_PARAM_MAX_NUM_KEY, QUERY_PARAM_MAX_NUM_VAL);
        params.put(QUERY_PARAM_INCLUDE_TARGET_DATA_KEY, QUERY_PARAM_INCLUDE_TARGET_DATA_VAL);
        return query(params);
    }

    public String query(Map<String, String> params) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        mResponseStatus = -1;
        mResponseBody = null;

        MultiPartFormBuilder formBuilder = new MultiPartFormBuilder();
        String contentType = "multipart/form-data";

        byte[] image = mData.getRequestContent();
        byte[] requestBody = formBuilder.build(image, params);
        final String dateStr = mData.getFormattedDate();

        HttpURLConnection conn = (HttpURLConnection) (new URL(getQueryUrl())).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setRequestProperty("Content-Type", contentType + "; boundary=" + formBuilder.getMultipartBoundary());
        conn.setRequestProperty("Authorization", getAuthorizationHeader("POST", requestBody, contentType, dateStr, "/v1/query"));
        conn.setRequestProperty("Accept", "application/json; charset=utf-8");
        conn.setRequestProperty("Date", dateStr);

        System.out.println("REQUESTBODY: " + requestBody.toString());
        conn.getOutputStream().write(requestBody);
        return readHttpResponse(conn);
    }


    private String getAuthorizationHeader(String verb, byte[] requestBody, String contentType, String date, String queryPath) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
//        return "VWS " + VUFORIA_CLIENT_ACCESS_KEY + ":" + sign("POST", requestBody, contentType, date, KConfig.getConfig().getPath());
        return "VWS " + API_KEY + ":" + sign("POST", requestBody, contentType, date, queryPath);
    }

    private String md5sum(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] theDigest = md.digest(content);
        return new String(Hex.encodeHex(theDigest));
    }

    private String sign(String verb, byte[] content, String contentType, String date, String requestPath) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {

        String httpVerb = verb;
        String stringToSign =
                httpVerb + "\n" +
                        md5sum(content) + "\n" +
                        contentType + "\n" +
                        date + "\n" +
                        requestPath;

        String signature = "";
        try {
            signature = SignatureBuilder.calculateRFC2104HMAC(API_SECRET, stringToSign);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return signature;
    }

    private String getQueryUrl() {
        KConfig config = KConfig.getConfig();
        String protocol = "https://";
//        return protocol + config.getServer() + config.getPath();
        return VUFORIA_QUERY_ENDPOINT;
    }


    private String readHttpResponse(HttpURLConnection conn) throws IOException {
        InputStream is = null;
        try {
            is = conn.getInputStream();
            mResponseStatus = conn.getResponseCode();
        } catch (IOException e) {
            try {
                mResponseStatus = conn.getResponseCode();
                is = conn.getErrorStream();
            } catch (IOException ex) {
                throw ex;
            }
        }

        BufferedReader bin = new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();
        String inputLine;
        while ((inputLine = bin.readLine()) != null)
            sb.append(inputLine);
        bin.close();

        mResponseBody = sb.toString();
        return mResponseBody;
    }

    public int getResponseStatus() {
        return mResponseStatus;
    }

    public String getResponseBody() {
        return mResponseBody;
    }
}
