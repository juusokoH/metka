/**************************************************************************************
 * Copyright (c) 2013-2015, Finnish Social Science Data Archive/University of Tampere *
 *                                                                                    *
 * All rights reserved.                                                               *
 *                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification,   *
 * are permitted provided that the following conditions are met:                      *
 * 1. Redistributions of source code must retain the above copyright notice, this     *
 *    list of conditions and the following disclaimer.                                *
 * 2. Redistributions in binary form must reproduce the above copyright notice,       *
 *    this list of conditions and the following disclaimer in the documentation       *
 *    and/or other materials provided with the distribution.                          *
 * 3. Neither the name of the copyright holder nor the names of its contributors      *
 *    may be used to endorse or promote products derived from this software           *
 *    without specific prior written permission.                                      *
 *                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND    *
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED      *
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE             *
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR   *
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES     *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;       *
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON     *
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT            *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                       *
 **************************************************************************************/

define(function (require) {
    'use strict';

    var resultParser = require('./../../resultParser');

    var filesContainerCreated = false;
    return function (options) {
        if (filesContainerCreated) {
            return;
        }
        filesContainerCreated = true;

        return {
            preCreate: function(options) {
                // Add data confs for non removed and removed files
                var fieldConf = options.dataConf.fields[options.field.key];
                var existingConf = $.extend(true, {}, fieldConf, {
                    key: "existingfiles"
                });
                var removedConf = $.extend(true, {}, fieldConf, {
                    key: "removedfiles"
                })

                options.dataConf.fields[existingConf.key] = existingConf;
                options.dataConf.fields[removedConf.key] = removedConf;
            },
            postCreate: function (options) {

                function partialRefresh() {
                    require('./../../server')('viewAjax', {
                        method: 'GET',
                        success: function (response) {
                            if (resultParser(response.result).getResult() === 'VIEW_SUCCESSFUL') {
                                // on browser, overwrite these fields only, since there might be other unsaved fields on page
                                ['files', 'studyvariables'].forEach(function (field) {
                                    options.data.fields[field] = response.data.fields[field];
                                });
                                options.$events.trigger('refresh.metka');
                            }
                        }
                    });
                }

                function view(requestOptions) {
                    require('./../../revisionModal')(options, requestOptions, 'STUDY_ATTACHMENT', partialRefresh, options.field.key, true);
                }

                var $filesContainer = $('<div>').appendTo(this);
                var $removedFilesContainer = $('<div>').appendTo(this);

                function addFileContainer($mount, cell) {
                    require('./../../inherit')(function (options) {
                        require('./../../container').call($mount, options);
                    })(options)({
                        data: {},
                        content: [{
                            "type": "COLUMN",
                            "columns": 1,
                            "rows": [{
                                "type": "ROW",
                                "cells": [cell]
                            }]
                        }]
                    });
                }

                addFileContainer($filesContainer, {
                    "type": "CELL",
                    "title": "Liitetyt tiedostot",
                    "field": $.extend(true, {}, options.field, {
                        "displayType": "REFERENCECONTAINER",
                        "key": "existingfiles",
                        "columnFields": [
                            "filepath",
                            "filelanguage"
                        ],
                        onClick: function (transferRow, replaceTr) {
                            view({
                                id: transferRow.value.split("-")[0],
                                no: transferRow.value.split("-")[1]
                            }, replaceTr);
                        },
                        onAdd: (!require('./../../isFieldDisabled')(options, 'DEFAULT')) ? function (originalEmptyData, addRow) {
                            require('./../../server')('create', {
                                data: JSON.stringify({
                                    type: 'STUDY_ATTACHMENT',
                                    parameters: {
                                        study: require('./../../../metka').id
                                    }
                                }),
                                success: function (response) {
                                    if (resultParser(response.result).getResult() === 'REVISION_CREATED') {
                                        view(response.data.key, addRow);
                                        partialRefresh();
                                    }
                                }
                            });
                        } : null
                    })
                });
                addFileContainer($removedFilesContainer, {
                    "type": "CELL",
                    "title": "Poistetut tiedostot",
                    "readOnly": true,
                    "field": $.extend(true, {}, options.field, {
                        "key": "removedfiles",
                        "displayType": "REFERENCECONTAINER",
                        "columnFields": [
                            "filepath",
                            "filedescription",
                            "filecomment"
                        ],
                        onClick: function (transferRow, replaceTr) {
                            view({
                                id: transferRow.value.split("-")[0],
                                no: transferRow.value.split("-")[1]
                            }, replaceTr);
                        }
                    })
                });

                $filesContainer.find('tbody').empty();
                $removedFilesContainer.find('tbody').empty();

                // TODO: This can be made using two separate REFERENCECONTAINERS and making relevant searchers to fill both (i.e. attachments that are not removed versus those that are)
                var rows = require('./../../data')(options).getByLang(options.defaultLang);
                if (rows) {
                    var i = 0;
                    (function processNextRow() {
                        if (i < rows.length) {
                            var transferRow = rows[i++];
                            if(transferRow.removed) {
                                processNextRow();
                                return;
                            }
                            require('./../../server')('/references/referenceStatus/{value}', transferRow, {
                                method: 'GET',
                                success: function (response) {
                                    if (response.exists) {
                                        options.$events.trigger('container-{key}-{lang}-push'.supplant({
                                            key: !response.removed ? "existingfiles" : "removedfiles",
                                            lang: options.defaultLang
                                        }), [transferRow]);
                                        //(!response.removed ? $filesContainer : $removedFilesContainer).find('.panel').parent().data('addRow')(transferRow);
                                    } else if(response.result === "REVISIONABLE_NOT_FOUND") {
                                        transferRow.removed = true;
                                    }
                                    processNextRow();
                                }
                            });
                        }
                    })(0);
                }
            }
        };
    };
});
