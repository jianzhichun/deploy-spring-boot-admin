'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy', [require('angular-ui-bootstrap/src/accordion')]);
global.sbaModules.push(module.name);


var service = null;

var deployState = {
    url: '/deploy',
    templateUrl: 'deploy/deploy.html',
    controller: ['$scope', function ($scope) {
        $scope.actionsContainers = service.getActionsContainers();
    }]
};

module.service('actionsContainerService', function () {
    var actionsContainers = [];
    service = this;

    this.register = function (actionsContainer) {
        actionsContainers.push(actionsContainer);
        $stateProviderRef.state('deploy.' + actionsContainer.name, actionsContainer);
        actionsContainers.sort(function (a1, a2) {
            return (a1.order || 0) - (a2.order || 0);
        });
    };
    this.getActionsContainers = function () {
        return actionsContainers;
    };
});

module.filter('removeSubString', function () {
    return function (input) {
        return input.replace('deploy.', '');
    };
});

var $stateProviderRef = null;

module.config(function ($stateProvider) {

    $stateProviderRef = $stateProvider;
    $stateProviderRef.state('deploy', deployState);
});

module.run(function (MainViews) {

    MainViews.register({
        title: 'Deploy',
        state: 'deploy',
        order: 100
    });
});
