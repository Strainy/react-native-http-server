package com.strainy.RNHttpServer;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.HashMap;

import android.util.Log;

public class RNHttpServerModule extends ReactContextBaseJavaModule {

    ReactApplicationContext reactContext;

    private static final String TAG = "RNHttpServer";

    private static final String DEFAULT_PORT_KEY = "DEFAULT_PORT";
    private static final String DEFAULT_TIMEOUT_KEY = "DEFAULT_TIMEOUT";
    private static final String SERVER_EVENT_ID_KEY = "SERVER_EVENT";

    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final String SERVER_EVENT_ID = "reactNativeHttpServerResponse";

    private static final String ERROR_SERVER_NOT_RUNNING = "ERROR_SERVER_NOT_RUNNING";
    private static final String ERROR_COULD_NOT_START = "ERROR_COULD_NOT_START";

    private int port;
    private int timeout;
    private Server server = null;


    public RNHttpServerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        port = DEFAULT_PORT;
        timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public String getName() {
        return "RNHttpServer";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DEFAULT_PORT_KEY, DEFAULT_PORT);
        constants.put(DEFAULT_TIMEOUT_KEY, DEFAULT_TIMEOUT);
        constants.put(SERVER_EVENT_ID_KEY, SERVER_EVENT_ID);

        return constants;
    }

    @ReactMethod
    public void init(ReadableMap options) {
        Log.d(TAG, "Initializing server...");

        if (options.hasKey("port")) {
            port = options.getInt("port");
        }

        if (options.hasKey("timeout")) {
            timeout = options.getInt("timeout");
        }
    }

    @ReactMethod
    public void start(Promise promise) {
        try {
            server = new Server(reactContext, port, timeout);
            server.start();
            port = server.getListeningPort();

            WritableMap args = Arguments.createMap();
            args.putString("hostname", server.getHostname());
            args.putString("port", String.valueOf(server.getListeningPort()));

            promise.resolve(args);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            promise.reject(ERROR_COULD_NOT_START, e);
        }
    }

    @ReactMethod
    public void stop() {
        if (server == null) { return; }

        server.stop();
        server = null;
        Log.d(TAG, "Server stopped.");
    }

    @ReactMethod
    public void setResponse(String uri, ReadableMap response) {
        if (server == null) { return; }
        server.setResponse(uri, response);
    }
}
