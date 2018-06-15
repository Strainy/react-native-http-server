package com.strainy.RNHttpServer;

import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server extends NanoHTTPD {

    private static final String TAG = "RNHttpServer";
    private static final String SERVER_EVENT_ID = "reactNativeHttpServerResponse";

    private int timeout;
    private ReactContext reactContext;
    private Map<String, ReadableMap> response;


    public Server(ReactContext context, int port, int timeout) {
        super("127.0.0.1", port);
        reactContext = context;
        this.timeout = timeout;
        response = new HashMap<>();
    }

    public void setResponse(String uri, ReadableMap response) {
        this.response.put(uri, response);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "Server receiving request.");

        Response.Status errorStatus = null;
        String errorText = "";
        int timer = 0;
        int interval = 500;
        WritableMap request = Arguments.createMap();
        ReadableMap response;

        // build request to send to react-native
        Method method = session.getMethod();
        request.putString("url", session.getUri());
        request.putString("method", method.toString());
        request.putMap("headers", this.convertToWritableMap(session.getHeaders()));
        request.putString("queryString", session.getQueryParameterString());

        // bit of complexity around getting the POST/PUT body...
        Map<String, String> files = new HashMap<String, String>();

        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(files);
                request.putString("data", files.get(files.keySet().toArray()[0]));
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
            }
        }

        // send request to react-native
        this.sendEvent(reactContext, SERVER_EVENT_ID, request);

        // wait for response
        while (!this.response.containsKey(session.getUri()) && (timer < timeout)) {
            try {
                Thread.sleep(interval);
                Log.d(TAG, "waiting for " + session.getUri() + " - " + timer + "ms");
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
                errorStatus = Response.Status.INTERNAL_ERROR;
                errorText = e.getMessage();
                break;
            }

            timer = timer + interval;
        }

        // if after waiting x seconds, no response has been logged, throw a 404 response
        if (!this.response.containsKey(session.getUri())) {
            errorStatus = Response.Status.NOT_FOUND;
            errorText = "Resource not found";
        }

        if (errorStatus != null) {
            return newFixedLengthResponse(errorStatus, MIME_PLAINTEXT, errorText);
        } else {
            response = this.response.get(session.getUri());
            this.response.remove(session.getUri()); // clear responses
        }

        Log.d(TAG, "Sending response for " + session.getUri());

        Response.Status status = Response.Status.valueOf(response.getString("status"));
        String type = response.getString("type");
        ReadableMap responseContent = response.getMap("content");

        Response res;
        if (responseContent.hasKey("data")) {
            String data = responseContent.getString("data");

            res = newFixedLengthResponse(status, type, data);
        } else if (responseContent.hasKey("filePath")) {
            String filePath = responseContent.getString("filePath");

            try {
                File file = new File(filePath);
                long responseLength = file.length();
                FileInputStream fileInputStream = new FileInputStream(file);

                res = newFixedLengthResponse(status, type, fileInputStream, responseLength);
            } catch (FileNotFoundException e) {
                errorStatus = Response.Status.INTERNAL_ERROR;
                errorText = "File not found: " + filePath;

                return newFixedLengthResponse(errorStatus, MIME_PLAINTEXT, errorText);
            }
        } else {
            errorStatus = Response.Status.INTERNAL_ERROR;
            errorText = "Response is neither file nor text, which is not supported";

            return newFixedLengthResponse(errorStatus, MIME_PLAINTEXT, errorText);
        }

        // Add headers to the response
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Max-Age", "3628800");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        res.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
        res.addHeader("Access-Control-Allow-Headers", "Authorization");

        return res;
    }

    // Convenience method for triggering events in react-native
    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    // Convert Java maps to WritableMaps for passing to JS
    private WritableMap convertToWritableMap(Map map) {
        WritableMap request = Arguments.createMap();

        // iterate over map keys and put values into WritableMap
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry entry : entrySet) {
            switch (entry.getValue().getClass().getName()) {
                case "java.lang.Boolean":
                    request.putBoolean(entry.getKey().toString(), (Boolean) entry.getValue());
                    break;
                case "java.lang.Integer":
                    request.putInt(entry.getKey().toString(), (Integer) entry.getValue());
                    break;
                case "java.lang.Double":
                    request.putDouble(entry.getKey().toString(), (Double) entry.getValue());
                    break;
                case "java.lang.String":
                    request.putString(entry.getKey().toString(), (String) entry.getValue());
                    break;
            }
        }

        return request;
    }
}
