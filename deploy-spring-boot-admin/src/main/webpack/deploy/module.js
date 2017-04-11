'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy', []);
global.sbaModules.push(module.name);

module.service('actionsContainerService', function () {
    var actionsContainers = [];
    var service = this;
    var actionsContainerViews = {
        '': {
            templateUrl: 'deploy/deploy.html',
            controller: ['$scope', function ($scope) {
                $scope.actionsContainers = service.getActionsContainers();
            }]
        }
    };

    this.register = function (actionsContainer) {
        actionsContainers.push(actionsContainer);
        actionsContainerViews[actionsContainer.name + '@deploy'] = actionsContainer;
        actionsContainers.sort(function (a1, a2) {
            return (a1.order || 0) - (a2.order || 0);
        });
        $stateProviderRef.state('deploy', {
            url: '/deploy',
            views: this.getActionContainerViews()
        });
    };
    this.getActionsContainers = function () {
        return actionsContainers;
    };
    this.getActionContainerViews = function () {
        return actionsContainerViews;
    };
});

var $stateProviderRef = null;

module.config(function ($stateProvider) {

    $stateProviderRef = $stateProvider;
});

module.run(function (MainViews) {

    MainViews.register({
        title: 'Deploy',
        state: 'deploy',
        order: 100
    });
});
