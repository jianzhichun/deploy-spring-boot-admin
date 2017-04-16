'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy-default', []);
global.sbaModules.push(module.name);
var defaultPlugin = {
    order: 100,
    templateUrl: 'deploy-actions-default/deploy.default.html',
    controller: 'deployDefaultCtrl'
};
global.sbaDeployPlugins = global.sbaDeployPlugins || {};
global.sbaDeployPlugins['default@deploy'] = defaultPlugin;

module.component('deployDefaultAction', require('./components/deployDefaultAction.js'));

module.controller('deployDefaultCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.plugin = defaultPlugin;

    $http.get('api/deploy/actions').then(function (response) {
        $scope.plugin.actions = response.data;
    });

    $scope.doAction = function (action) {
        action.showSpin = true;
        $http.post('api/deploy/doAction/', [action]).then(function (response) {
            action.results = response.data;
            action.showSpin = false;
        });
    };

    $scope.clearResult = function (action) {
        action.results = '';
    };

}]);


