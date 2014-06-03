(function() {
    'use strict';
    /**
     * Component builder that returns a button based on button-configuration as specified in the
     * GUI-configuration. These buttons should be used on view page to provide user controls related to
     * the whole revision or revisionable object.
     *
     * @param button Button configuration
     * @returns {*|jQuery|HTMLElement} Input of type button and with title provided by the given configuration.
     */
    GUI.Components.viewButton = function (button) {
        return $('<button>', {
            type: 'button',
            'class': 'btn btn-primary'
        }).text(MetkaJS.L10N.localize(button, 'title'));
    }
}());