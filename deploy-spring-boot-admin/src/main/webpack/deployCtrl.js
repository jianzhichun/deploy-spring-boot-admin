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
    $scope.a = [];

    $http.get('api/deploy/actions').then(function(response) {
        $scope.a = response.data;
        $scope.actions = Object.keys(response.data);
    });

    $scope.takeAction = function(action) {
        $scope.a[action].isSpinnerShow = true;
        $http.get('api/deploy/doAction/' + action).then(function(response) {
            //.replace(/\r\n|\n|\r/gm, 'eval("<br/>")')
            $scope.a[action].result = response.data;
            $scope.a[action].isResultReturn = true;
            $scope.a[action].isSpinnerShow = false;
        });
    };

    $scope.clearResult = function(action) {
        $scope.a[action].isResultReturn = false;
        $scope.a[action].result = '';
    };

};