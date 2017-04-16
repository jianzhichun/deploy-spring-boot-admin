'use strict';

module.exports = {
    bindings: {
        action: '<action'
    },
    controller: function () {
        'ngInject';
        var ctrl = this;

        ctrl.editStep = function(step) {
            step.edit = true;
            step.exec += ([''] || step.args).join(' ');
            step.args = null;
        };
    },
    template: require('./defaultAction.tpl.html')
};
