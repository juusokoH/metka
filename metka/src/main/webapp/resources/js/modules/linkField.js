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

    return function ($field, options, lang) {

        if (options.fieldOptions.type === 'REFERENCE') {

            var reference = require('./utils/getPropertyNS')(options, 'dataConf.references', options.fieldOptions.reference);
            if (!reference) {
                return
            }
            if (!(reference.type === 'REVISIONABLE' || reference.type === 'REVISION')) {
                // TODO: merge code with other users of ´reference´ module
                log('No linkable data');
            } else {
                var value = require('./data')(options).getByLang(lang);
                require('./reference').optionsByPath(options.field.key, options, lang, function (listOptions) {
                    if(!listOptions) {
                        return;
                    }

                    var option = listOptions.find(function (option) {
                        return option.value === value;
                    });

                    if (option) {
                        $field.append($('<a>', {
                            text: '{label} - {value}'.supplant({
                                label: MetkaJS.L10N.get('type.{target}.title'.supplant(reference)),
                                value: require('./selectInputOptionText')(option)
                            }),
                            href: require('./url')('view', {
                                PAGE: reference.target,
                                id: reference.type == 'REVISIONABLE' ? option.value : option.value.split("-")[0],
                                no: reference.type == 'REVISIONABLE' ? '' : option.value.split("-")[1]
                            })
                        }));
                    }
                })();
            }
        }
    };
});

