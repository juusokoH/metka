define(function (require) {
    var addRow;
    var $query;
    var options = {
        header: MetkaJS.L10N.get('topmenu.expert'),
        content: [
            {
                "type": "COLUMN",
                "columns": 2,
                "rows": [
                    {
                        "type": "ROW",
                        "cells": [
                            {
                                "type": "CELL",
                                "title": "Hakulause",
                                "colspan": 1,
                                "field": {
                                    "displayType": "STRING",
                                    "key": "search",
                                    "multiline": true
                                },
                                create: function () {
                                    $query = this.find('textarea');
                                }
                            },
                            {
                                "type": "CELL",
                                "title": "Tallennetut haut",
                                "colspan": 1,
                                "elementId": "savedSearches",
                                "field": {
                                    "readOnly": true,
                                    "displayType": "CONTAINER",
                                    "key": "savedSearches",
                                    "columnFields": [
                                        "name",
                                        "savedBy",
                                        "savedAt",
                                        "remove"
                                    ]
                                },
                                create: function (options) {
                                    addRow = options.addRow;
                                    this
                                        .find('table')
                                            .addClass('table-hover')
                                            .find('tbody')
                                                .on('click', 'tr', function () {
                                                    $query.val($(this).data('query'));
                                                });
                                    require('./../server')('/expertSearch/list', {
                                        success: function (data) {
                                            data.queries.forEach(addRow);
                                        }
                                    });
                                }
                            }
                        ]
                    }
                ]
            }
        ],
        buttons: [{
            "&title": {
                "default": "Tee haku"
            },
            create: function () {
                this
                    .click(function () {
                        require('./../server')('/expertSearch/query', {
                            data: JSON.stringify({
                                query: require('./../data').get(options, 'search')
                            }),
                            success: function (data) {
                                require('./../data').set(options, 'searchResults', data.results.map(function (result) {
                                    return {
                                        title: result.title,
                                        type: MetkaJS.L10N.get('type.{type}.title'.supplant(result)),
                                        TYPE: result.type,
                                        id: result.id,
                                        revision: result.revision,
                                        state: MetkaJS.L10N.get('search.result.state.{state}'.supplant(result))
                                    };
                                }));
                                $('#searchResultTable').remove();
                                var $field = require('./../field').call($('<div>'), {
                                    dataConf: {
                                        fields: {
                                            title: {
                                                type: 'STRING'
                                            },
                                            type: {
                                                type: 'STRING'
                                            },
                                            id: {
                                                type: 'INTEGER'
                                            },
                                            revision: {
                                                type: 'INTEGER'
                                            },
                                            state: {
                                                type: 'STRING'
                                            }
                                        }
                                    },
                                    style: 'primary',
                                    readOnly: true,
                                    field: {
                                        displayType: 'CONTAINER',
                                        "key": "searchResults",
                                        "columnFields": [
                                            "title",
                                            "type",
                                            "id",
                                            "revision",
                                            "state"
                                        ]
                                    }
                                })
                                    .attr('id', 'searchResultTable');

                                $field.find('table')
                                    .addClass('table-hover')
                                    .find('tbody')
                                    .on('click', 'tr', function () {
                                        var $this = $(this);
                                        require('./../assignUrl')('view', {
                                            id: $this.data('id'),
                                            revision: $this.data('revision'),
                                            page: $this.data('TYPE').toLowerCase()
                                        });
                                    });

                                $field.find('.panel-heading')
                                    .text(MetkaJS.L10N.get('search.result.title'))
                                    .append($('<div class="pull-right">')
                                        .text(MetkaJS.L10N.get('search.result.amount').supplant(data.results)));

                                $('.content').append($field);
                            }
                        });
                    })
            }
        }, {
            "&title": {
                "default": "Tyhjennä"
            },
            create: function () {
                this.click(function () {
                    $query
                        .val('')
                        .change();
                });
            }
        }, {
            "&title": {
                "default": "Tallenna haku"
            },
            create: function () {
                this
                    .click(function () {
                        require('./../modal')({
                            title: 'Tallenna haku',
                            body: require('./../container').call($('<div>'), {
                                dataConf: {},
                                content: [{
                                    type: 'COLUMN',
                                    columns: 1,
                                    rows: [
                                        {
                                            "type": "ROW",
                                            "cells": [
                                                {
                                                    "type": "CELL",
                                                    "title": "Nimi",
                                                    "colspan": 1,
                                                    "field": {
                                                        "displayType": "STRING",
                                                        "key": "title"
                                                    }
                                                }
                                            ]
                                        }
                                    ]
                                }]
                            }),
                            buttons: [{
                                "&title": {
                                    "default": 'Tallenna'
                                },
                                create: function (options) {
                                    this
                                        .click(function () {
                                            require('./../server')('/expertSearch/save', {
                                                data: JSON.stringify({
                                                    query: require('./../data').get(options, 'search'),
                                                    title: require('./../data').get(options, 'title')
                                                }),
                                                success: function (data) {
                                                    data.name = data.title;
                                                    delete data.title;
                                                    addRow(data);
                                                }
                                            });
                                        });
                                }
                            }, {
                                type: 'CANCEL'
                            }]
                        });
                    });
            }
        }],
        data: {
        },
        dataConf: {
            fields: {
                name: {
                    type: "STRING"
                },
                savedBy: {
                    type: "STRING"
                },
                savedAt: {
                    type: "DATE"
                },
                remove: {
                    type: "BUTTON"
                }
            }
        }
    };
    return options;
});