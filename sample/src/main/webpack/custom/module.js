'use strict';
var angular = require('angular');
var module = angular.module('sba-deploy-custom', []);
global.sbaModules.push(module.name);

var customDeployPlugin = {
    order: 150,
    templateUrl: 'custom/deploy.custom.html',
    controller: 'deployCustomCtrl'
};
global.sbaDeployPlugins = global.sbaDeployPlugins || {};
global.sbaDeployPlugins['custom@deploy'] = customDeployPlugin;


module.controller('deployCustomCtrl', ['$scope', '$http', function ($scope, $http) {
    var paramIndex = 0;
    var actionIndex = 0;
    $scope.customAction = {
        params: [],
        steps: [],
        addParam: function () {
            $scope.customAction.params[paramIndex] = { placeholder: 'undefined' };
            paramIndex++;
        },
        delParam: function (key) {
            $scope.customAction.params.splice(key, 1);
            paramIndex--;
        },
        addStep: function () {
            $scope.customAction.steps[actionIndex] = { exec: '' };
            actionIndex++;
        },
        delStep: function (key) {
            $scope.customAction.steps.splice(key, 1);
            actionIndex--;
        },
        doAction: function () {
            $scope.customAction.showSpin = true;
            var action = { name: 'custom', steps: [] };
            angular.forEach($scope.customAction.steps, function (step) {
                var tempStep = { exec: step.exec };
                angular.forEach($scope.customAction.params, function (param) {
                    tempStep.exec = tempStep.exec.replace(new RegExp('\\$' + param.placeholder, 'g'), param.value);
                });
                this.push(tempStep);
            }, action.steps);
            $http.post('api/deploy/doAction/', [action]).then(function (response) {
                $scope.customAction.results = response.data;
                $scope.customAction.showSpin = false;
            });
        },

        clearResult: function () {
            $scope.customAction.results = '';
        }
    };

}]);