'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy', [require('angular-ui-bootstrap/src/accordion')]);
global.sbaModules.push(module.name);
global.sbaDeployPlugins = global.sbaDeployPlugins || {};
global.sbaDeployPlugins[''] = {
    templateUrl: 'deploy/deploy.html',
    controller: ['$scope', function ($scope) {
        $scope.plugins = [];
        angular.forEach(global.sbaDeployPlugins, function (value, key) {
            if (key === '') return;
            value.name = key.replace('@deploy', '');
            this.push(value);
        }, $scope.plugins);
    }]
};

module.config(function ($stateProvider) {

    $stateProvider.state('deploy', {
        url: '/deploy',
        views: global.sbaDeployPlugins
    });
});

module.run(function (MainViews) {

    MainViews.register({
        title: 'Deploy',
        state: 'deploy',
        order: 100
    });
});
