/*jslint node: true */
"use strict";

var spark =require('spark');

spark.on('login', function() {
  var myDevices = spark.listDevices();

  myDevices.then(
    function(devices) {
        //console.log("My device: ", devices[0]);
        var core = devices[0];
        // test sending signal
        //console.log('- variables: ' + core.variables);
        /*core.stopSignal(function(err, data) {
            if (err) {
                console.log('Error sending a signal to the core:', err);
            } else {
                console.log('Core signal sent successfully:', data);
            }
        });*/
        core.getVariable('weight', function(err, data) {
          if (err) {
            console.log('An error occurred while getting attrs:', err);
          } else {
            console.log('Device attr retrieved successfully:', data);
          }
        });
        // test calling a function in the core
        /*core.callFunction('blinky', 'on', function(err, data) {
            if (err) {
                console.log("An error occurred: ", err);
            } else {
                console.log("Function called successfully: ", data);
            }
        });*/
    }
  );
});

spark.login({ username: 'mchoun95@mit.edu', password: 'mark1995' });