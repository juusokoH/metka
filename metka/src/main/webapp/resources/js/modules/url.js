// same as .url method, except also navigates to the url

define(function (require) {
    'use strict';
    return function (key, extend) {
        var metka = require('./../metka');
        return metka.contextPath + (function () {
            if (key[0] === '/') {
                return key;
            } else {
                return '/' + {
                    approve: 'revision/ajax/approve',
                    compareRevisions: 'history/revisions/compare',
                    create: 'revision/ajax/create',
                    download: 'download/{id}/{revision}',
                    edit: '{page}/edit/{id}',
                    expertSearch: 'expertSearch/',
                    fileEdit: 'file/save/{value}',
                    fileSave: 'file/save',
                    fileUpload: 'file/upload',
                    listRevisions: 'history/revisions/{id}',
                    next: 'next/{page}/{id}',
                    options: 'references/collectOptionsGroup',
                    prev: 'prev/{page}/{id}',
                    remove: 'remove/{page}/{type}/{id}',
                    save: 'revision/ajax/save',
                    search: '{page}/ajax/search',
                    view: 'revision/view/{page}/{id}/{revision}'
                }[key];
            }
        })().supplant($.extend({}, metka, extend));
    };
});
