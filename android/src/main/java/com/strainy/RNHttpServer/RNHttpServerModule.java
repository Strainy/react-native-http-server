package com.strainy.RNHttpServer;

import com.strainy.RNHttpServer.Server;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.HashMap;

import android.support.annotation.Nullable;
import android.util.Log;

public class RNHttpServerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  ReactApplicationContext reactContext;

  private static final String TAG = "RNHttpServer";

  private static final String DEFAULT_PORT_KEY = "DEFAULT_PORT";
  private static final String DEFAULT_TIMEOUT_KEY = "DEFAULT_TIMEOUT";
  private static final String SERVER_EVENT_ID_KEY = "SERVER_EVENT";

  private static final int DEFAULT_PORT = 8080;
  private static final int DEFAULT_TIMEOUT = 30000;
  private static final String SERVER_EVENT_ID = "reactNativeHttpServerResponse";

  private int _port;
  private int _timeout;

  private Server _server = null;

  private Callback _success;
  private Callback _failure;

  public RNHttpServerModule(ReactApplicationContext reactContext) {
    super(reactContext);

    _port = DEFAULT_PORT;
    _timeout = DEFAULT_TIMEOUT;

    reactContext.addLifecycleEventListener(this);

    this.reactContext = reactContext;

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
  public void init(ReadableMap options, Callback success, @Nullable Callback failure) {

    Log.d(TAG, "Initialising server...");

    if(options.hasKey("port")) {
      _port = options.getInt("port");
    }

    if(options.hasKey("timeout")) {
      _timeout = options.getInt("timeout");
    }

    start(success, failure);

  }

  @ReactMethod
  public void start(@Nullable Callback success, @Nullable Callback failure) {

      try {

        _server = new Server(reactContext, _port, _timeout);
        _server.start();

        if(success != null) {
          success.invoke();
        }

      }
      catch(Exception e) {
        Log.e(TAG, e.getMessage());

        if(failure != null) {
          failure.invoke(e.getMessage());
        }

      }

  }

  @ReactMethod
  public void stop() {
    Log.d(TAG, "Server Stopped.");
    _server.stop();
    _server = null;
  }

  @ReactMethod
  public void setResponse(String uri, ReadableMap response) {
    if(_server != null) {
      _server.setResponse(uri, response);
    }
  }

  @ReactMethod
  public String getHostName() {
    if(_server != null) {
      Log.d(TAG, _server.getHostname());
      return _server.getHostname();
    }
    else {
      return "not defined";
    }
  }

  /* Shut down the server if app is destroyed or paused */
  @Override
  public void onHostResume() {
    //we can restart the server here as the success callback is not needed since an event is registered
    start(null, null);
  }

  @Override
  public void onHostPause() {
    stop();
  }

  @Override
  public void onHostDestroy() {
    stop();
  }

}
