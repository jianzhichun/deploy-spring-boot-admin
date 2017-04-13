'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy-default', []);
global.sbaModules.push(module.name);

module.component('sbaDefaultAction', require('./components/defaultAction.js'));

var defaultActionsContainer = {
    name: 'DEFAULT',
    order: 100,
    templateUrl: 'deploy-actions-default/deploy.default.html',
    controller: 'deployDefaultCtrl',
    selectedAction: null
};

module.controller('deployDefaultCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.error = null;
    $scope.actionsContainer = defaultActionsContainer;

    $http.get('api/deploy/actions').then(function (response) {
        $scope.actionsContainer.actions = response.data;
    });

}]);

module.run(function (actionsContainerService) {
    actionsContainerService.register(defaultActionsContainer);
});
