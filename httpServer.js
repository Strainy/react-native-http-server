/**
 * @providesModule react-native-http-server
 */
'use strict';

import {DeviceEventEmitter} from 'react-native';
import {NativeModules} from 'react-native';

const validStatusCodes = [
    'ACCEPTED', 'BAD_REQUEST', 'CREATED', 'FORBIDDEN', 'INTERNAL_ERROR', 'METHOD_NOT_ALLOWED',
    'NO_CONTENT', 'NOT_FOUND', 'NOT_MODIFIED', 'OK', 'PARTIAL_CONTENT', 'RANGE_NOT_SATISFIABLE',
    'REDIRECT', 'UNAUTHORIZED',
];

const nativeServerModule = NativeModules.RNHttpServer;

module.exports = {
    init: (options, callback) => {
        // listen for new requests and retrieve appropriate response
        DeviceEventEmitter.addListener('reactNativeHttpServerResponse', async function (request) {
            let response = await new Promise((resolve) => { callback(request, resolve); })

            //Validate status code
            if (validStatusCodes.indexOf(response.status) === -1) {
                throw new Error("Invalid response status code specified in RNHttpServer options.");
            }

            if (response.type == null) {
                response.type = "text/plain";
            }

            if (response.data == null) {
                response.data = "";
            }

            if (response.headers == null) {
                response.headers = {};
            }

            nativeServerModule.setResponse(request.url, response);
        });

        nativeServerModule.init(options);
    },

    start: nativeServerModule.start,

    stop: nativeServerModule.stop,
}
