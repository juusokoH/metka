define(function (require) {
    'use strict';

    var metka = require('./../metka');
    require('./topMenu');

    require('./uiLocalization');
    document.title = MetkaJS.L10N.get('page.title');

    var options = require('./optionsBase')();

    var getPropertyNS = require('./utils/getPropertyNS');

    require('./pageConfig')(options, function () {
        $('body')
            .append($('<div class="wrapper">')
                .append($('<div class="content container">')
                    // Inner elements may refresh page by triggering 'refresh.metka' event.
                    // If event is not captured before it propagates here, page will be re-rendered.
                    .on('refresh.metka', function () {
                        metka.id = getPropertyNS(options, 'data.key.id');
                        metka.no = getPropertyNS(options, 'data.key.no');

                        // (re-)render page
                        var $this = $(this)
                            .empty()
                            .append(require('./header')(options.header));
                        require('./container').call($this, options);
                        require('./buttonContainer').call($this, options);

                        //$(window).off('beforeunload', require('./onBeforeUnload'));
                        return false;
                    })
                    // trigger once here immediately
                    .trigger('refresh.metka')));
    });
});
