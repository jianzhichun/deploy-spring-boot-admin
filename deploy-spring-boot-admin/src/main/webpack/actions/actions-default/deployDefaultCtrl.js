/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

// require('./deploy.css');
module.exports = function($scope, $http) {
    'ngInject';
    $scope.error = null;
    $scope.actions = [];

    $http.get('api/deploy/actions').then(function(response) {
        $scope.actions = response.data;
    });

    $scope.doAction = function(action) {
        action.isSpinnerShow = true;
        $http.get('api/deploy/doAction/' + action).then(function(response) {
            action.results = response.data;
            action.isResultReturn = true;
            action.isSpinnerShow = false;
        });
    };

    $scope.clearResult = function(action) {
        $scope.a[action].isResultReturn = false;
        $scope.a[action].result = '';
    };

};