'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy-default', []);
global.sbaModules.push(module.name);

module.controller('deployDefaultCtrl', require('./deployDefaultCtrl.js'));

module.run(function(actionsContainerService) {

    actionsContainerService.register({
        name: 'Default',
        order: 100,
        templateUrl: 'deploy/actions/actions-default/deploy.default.html',
        controller: 'deployDefaultCtrl'
    });
});

// module.exports = this.module;