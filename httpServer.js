/**
 * @providesModule react-native-http-server
 */
 'use strict';

 import { DeviceEventEmitter } from 'react-native';
 import { NativeModules } from 'react-native';
 var RNHS = NativeModules.RNHttpServer;

 var validStatusCodes = [ 'ACCEPTED',
                          'BAD_REQUEST',
                          'CREATED',
                          'FORBIDDEN',
                          'INTERNAL_ERROR',
                          'METHOD_NOT_ALLOWED',
                          'NO_CONTENT',
                          'NOT_FOUND',
                          'NOT_MODIFIED',
                          'OK',
                          'PARTIAL_CONTENT',
                          'RANGE_NOT_SATISFIABLE',
                          'REDIRECT',
                          'UNAUTHORIZED' ];

 module.exports = {

   // create the server and listen/respond to requests
   create: function(options, callback) {

     //Validate port
     if(options.port == 80){
       throw "Invalid server port specified. Port 80 is reserved.";
     }

     // fire up the server
     RNHS.init(options, function() {

       // listen for new requests and retrieve appropriate response
       DeviceEventEmitter.addListener('reactNativeHttpServerResponse', function(request) {

         var success = true;

         var promise = new Promise(function(resolve, reject){
           callback(request, resolve);
         })
         .then(function(response){

           //Validate status code
           if(validStatusCodes.indexOf(response.status) === 0){
             success = false;
             throw "Invalid response status code specified in RNHttpServer options.";
           }

           //Set default MIME_TYPE
           if(response.type === null){
             response.type = "text/plain";
           }

           //Set default data field to zero length string
           if(response.data === null){
             response.data = "";
           }

           if(response.headers === null){
             response.headers = {};
           }

           if(success){
             RNHS.setResponse(request.url, response);
           }

         });

       });

     }, function(e){
       throw "Could not initialise server: " + e;
     });

   },

   // attempt to start the instance of the server - returns a promise object that will be rejected or approved
   start: function() {

     var promise = new Promise(function(resolve, reject){

       RNHS.start(function(){
         resolve();
       }, function(){
         reject();
       });

     });

     return promise;

   },

   // effectively pause the instance of the server
   stop: function() {

     RNHS.stop();

   }

 }
