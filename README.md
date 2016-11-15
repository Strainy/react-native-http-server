# react-native-http-server

HTTP Server for [React Native](https://github.com/facebook/react-native)

Android only for now.

## Install

```shell
npm install --save react-native-http-server
```

## Automatically link

#### With React Native 0.27+

```shell
react-native link react-native-http-server
```

#### With older versions of React Native

You need [`rnpm`](https://github.com/rnpm/rnpm) (`npm install -g rnpm`)

```shell
rnpm link react-native-http-server
```

## Manually link

- in `android/app/build.gradle`:

```diff
dependencies {
    ...
    compile "com.facebook.react:react-native:+"  // From node_modules
+   compile project(':react-native-http-server')
}
```

- in `android/settings.gradle`:

```diff
...
include ':app'
+ include ':react-native-http-server'
+ project(':react-native-http-server').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-http-server/android')
```

#### With React Native 0.29+

- in `MainApplication.java`:

```diff
+ import com.strainy.RNHttpServer.RNHttpServer;

  public class MainApplication extends Application implements ReactApplication {
    //......

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
+         new RNHttpServer(),
          new MainReactPackage()
      );
    }

    ......
  }
```

#### With older versions of React Native:

- in `MainActivity.java`:

```diff
+ import com.strainy.RNHttpServer.RNHttpServer;

  public class MainActivity extends ReactActivity {
    ......

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
+       new RNHttpServer(),
        new MainReactPackage()
      );
    }
  }
```


## Release Notes

See [CHANGELOG.md](https://github.com/strainy/react-native-http-server/blob/master/CHANGELOG.md)

## Example

```js

var httpServer = require('react-native-http-server');

var options = {
  port: 8080, // note that 80 is reserved on Android - an exception will be thrown
};

httpServer.start(options, function(request){

  console.log(request);

  var response = "Server up and running!";

  return response;

});

```
