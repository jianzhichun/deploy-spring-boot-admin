'use strict';

var angular = require('angular');

var module = angular.module('sba-deploy', []);
global.sbaModules.push(module.name);

module.controller('deployCtrl', require('./deployCtrl.js'));

module.config(function ($stateProvider) {
  $stateProvider.state('deploy', {
    url: '/deploy',
    templateUrl: 'deploy/deploy.html',
    controller: 'deployCtrl'
  });
});

module.run(function (MainViews) {
  MainViews.register({
    title: 'Deploy',
    state: 'deploy',
    order: 100
  });
});
