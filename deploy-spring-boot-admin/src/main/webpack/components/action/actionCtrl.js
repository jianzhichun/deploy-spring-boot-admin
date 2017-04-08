'use strict';

require('./action.css');
module.exports =  {
    bindings: {
        action:'<action'
    }, 
    controller:function ($scope, $http) {
        'ngInject';
        var ctrl = this;
        $scope.content = 'This is content';
        $scope.action = ctrl.action;
        $scope.takeAction = function() {
            $http.get('api/deploy/doAction/' + ctrl.action).then(function (response) {
                $scope.result = response.data;
            }); 
        }; 
    }, 
    template:require('./action.tpl.html')
};