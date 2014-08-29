define(function (require) {
    'use strict';
    return function (options, onLoad) {
        if (location.pathname.split('/').indexOf('search') !== -1) {
            $.extend(options, {
                header: MetkaJS.L10N.get('type.STUDY_VARIABLES.search'),
                content: [
                    {
                        "type": "COLUMN",
                        "columns": 1,
                        "rows": [{
                            "type": "ROW",
                            "cells": [
                                {
                                    "type": "CELL",
                                    "field": {},
                                    create: function create(options) {
                                        require('./../searchResultContainer')(
                                            '/study/studiesWithVariables',
                                            function () {
                                                return '';
                                            },
                                            function (data) {
                                                return data.studies;
                                            }, function (result) {
                                                return result;
                                            }, {
                                                title: {
                                                    type: 'STRING'
                                                },
                                                id: {
                                                    type: 'INTEGER'
                                                }
                                            }, ['title'], function (transferRow) {
                                                require('./../assignUrl')('view', {
                                                    id: transferRow.fields.id.values.DEFAULT.current,
                                                    no: '???'
                                                });
                                            }
                                        );
                                    }
                                }
                            ]
                        }]
                    }
                ],
                buttons: [],
                data: {},
                dataConf: {}
            });
        } else {
            $.extend(options, {
                "content": [
                    {
                        "type": "TAB",
                        "title": "Perusnäkymä",
                        "content": [
                            {
                                "type": "COLUMN",
                                "columns": 1,
                                "rows": [
                                    {
                                        "type": "ROW",
                                        "cells": [
                                            {
                                                "type": "CELL",
                                                "field": {
                                                    "key": "custom_studyVariablesBasic"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "type": "TAB",
                        "title": "Ryhmitelty näkymä",
                        "content": [
                            {
                                "type": "COLUMN",
                                "columns": 1,
                                "rows": [
                                    {
                                        "type": "ROW",
                                        "cells": [
                                            {
                                                "type": "CELL",
                                                "field": {
                                                    "key": "custom_studyVariablesGrouped"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        "type": "TAB",
                        "title": "Muuttujien ryhmittely",
                        "content": [
                            {
                                "type": "COLUMN",
                                "columns": 1,
                                "rows": [
                                    {
                                        "type": "ROW",
                                        "cells": [
                                            {
                                                "type": "CELL",
                                                "field": {
                                                    "key": "custom_studyVariablesGrouping"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            });
        }
        onLoad();
    };

});