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

module.exports = {
    bindings: {
        action: '<action'
    },
    controller: function ($http) {
        'ngInject';
        var ctrl = this;
        ctrl.doAction = function (action) {
            action.isSpinnerShow = true;
            $http.post('api/deploy/doAction/', [action]).then(function (response) {
                action.results = response.data;
                action.isResultReturn = true;
                action.isSpinnerShow = false;
            });
        };

        ctrl.editStep = function(step) {
            step.edit = true;
            step.exec += ([''] || step.args).join(' ');
            step.args = null;
        };

        ctrl.clearResult = function (action) {
            action.isResultReturn = false;
            action.results = '';
        };
    },
    template: require('./defaultAction.tpl.html')
};
