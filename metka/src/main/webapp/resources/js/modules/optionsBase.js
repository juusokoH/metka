define(function(require) {
    'use strict';

    return function(parent) {
        return {
            header: 'Metka',
            parent: parent,
            isReadOnly: (parent && parent.isReadOnly ? parent.isReadOnly : require('./functions/isReadOnly')),
            content: [],
            body: null,
            data: {},
            dataConf: {},
            $events: $({}),
            defaultLang: 'DEFAULT'
        }
    }
});