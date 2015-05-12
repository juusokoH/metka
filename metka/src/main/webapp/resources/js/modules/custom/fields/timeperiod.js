define(function (require) {
    'use strict';

    return function (options) {
        delete options.field.displayType;

        return {
            preCreate: function (options) {
                var $elem = this;

                var curVal = require('./../../data')(options)("timeperiodtext").getByLang("DEFAULT");
                options.required = !curVal;

                options.$events.on('data-changed-{key}-{lang}'.supplant({
                                key: "timeperiodtext",
                                lang: "DEFAULT"
                            }), function(e, value) {
                    options.required = !value;
                    options.$events.trigger('label-update-{key}-{lang}'.supplant({
                        key: 'timeperiod',
                        lang: 'DEFAULT'
                    }));
                });
            }
        }
    };
});
