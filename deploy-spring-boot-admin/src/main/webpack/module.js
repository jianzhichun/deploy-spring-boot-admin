'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy', []);
global.sbaModules.push(module.name);

module.service('actionsContainerService', function () {
    var actionsContainers = [];
    var actionsContainerViews = {
        '': {
            templateUrl: 'deploy/deploy.html'
        }
    };
    this.register = function (actionsContainer) {
        actionsContainers.push(actionsContainer);
        actionsContainerViews[actionsContainer.name] = actionsContainer;
        actionsContainers.sort(function (a1, a2) {
            return (a1.order || 0) - (a2.order || 0);
        });
    };
    this.getActionsContainers = function () {
        return actionsContainers;
    };
    this.getActionContainerViews = function(){
        return actionsContainerViews;
    };
});

var $stateProviderRef = null;

module.config(function($stateProvider) {
    $stateProviderRef = $stateProvider;
});

module.run(function($rootScope,MainViews,actionsContainerService) {
    $stateProviderRef.state('deploy', {
        url: '/deploy',
        templateUrl: 'deploy/deploy.html',
        views: actionsContainerService.getActionContainerViews()
        // controller: 'deployCtrl'
    });
    $rootScope.actionsContainers = actionsContainerService.getActionsContainers();
    MainViews.register({
        title: 'Deploy',
        state: 'deploy',
        order: 100
    });
});

// module.exports = this.module;