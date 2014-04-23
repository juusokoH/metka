// TODO: clean this script to include only needed functionality
$(document).ready(function(){
    /*$(".sortableTable").tablesorter();*/

    // TODO: localize calendar texts
    jQuery(function($){
        $.datepicker.regional['fi'] = {
            closeText: 'Sulje',
            prevText: '&laquo;Edellinen',
            nextText: 'Seuraava&raquo;',
            currentText: 'T&auml;n&auml;&auml;n',
            monthNames: ['Tammikuu','Helmikuu','Maaliskuu','Huhtikuu','Toukokuu','Kes&auml;kuu','Hein&auml;kuu','Elokuu','Syyskuu','Lokakuu','Marraskuu','Joulukuu'],
            monthNamesShort: ['Tammi','Helmi','Maalis','Huhti','Touko','Kes&auml;','Hein&auml;','Elo','Syys','Loka','Marras','Joulu'],
            dayNamesShort: ['Su','Ma','Ti','Ke','To','Pe','Su'],
            dayNames: ['Sunnuntai','Maanantai','Tiistai','Keskiviikko','Torstai','Perjantai','Lauantai'],
            dayNamesMin: ['Su','Ma','Ti','Ke','To','Pe','La'],
            weekHeader: 'Vk',
            dateFormat: 'yy-mm-dd',
            firstDay: 1,
            isRTL: false,
            showMonthAfterYear: false,
            yearSuffix: ''};
        $.datepicker.setDefaults($.datepicker.regional['fi']).setDefaults({beforeShow: function(i) {
            if($(i).attr('readonly')) return false;
        }});
    });

    $("input[type=radio][name=language]").on("click", function() {
        var language = $(this).val();
        $(".translationSv").hide();
        $(".translationEn").hide();

        if ( language == "fi" ) {
            toggleFinnishTranslations(false);
        } else if ( language == "en" ) {
            $(".translationEn").show();
            toggleFinnishTranslations(true);
            $(".translationBorder").addClass("translationEnBorder");
            $("#materialNameEnInput").attr("disabled", false);
        } else if ( language == "sv" ) {
            $(".translationSv").show();
            toggleFinnishTranslations(true);
            $(".translationBorder").addClass("translationSvBorder");
        }
    });

    /*function toggleFinnishTranslations(hide) {
        $(".translationFi").find("input").attr("disabled", hide);
        $(".translationFi").find("textarea").attr("disabled", hide);
        $(".translationFi").find("select").attr("disabled", hide);
        if ( hide ) {
            $(".rowContainer:not(.containsTranslations)").hide();
            $(".materialDataSetContainer:not(.translated), .materialDataSetTextareaContainer:not(.translated)").hide();
            $(".studyLevelDataSetContainer:not(.translated), .studyLevelDataSetContainer:not(.translated)").hide();
            $("#normalDesktop").hide();
            $("#translatorDesktop").show();
            $(".translationBorder").removeClass("translationEnBorder");
            $(".translationBorder").removeClass("translationSvBorder");
            $("#studyLevelData").find(".translationFi").find("a").hide();
        } else {
            $(".rowContainer:not(.containsTranslations)").show();
            $(".materialDataSetContainer:not(.translated), .materialDataSetTextareaContainer:not(.translated)").show();
            $(".studyLevelDataSetContainer:not(.translated), .studyLevelDataSetTextareaContainer:not(.translated)").show();
            $("#normalDesktop").show();
            $("#translatorDesktop").hide();
            $(".translationBorder").removeClass("translationSvBorder");
            $(".translationBorder").removeClass("translationEnBorder");
            $("#studyLevelData").find(".translationFi").find("a").show();
        }
    }*/

    /*$(".helpImage").on("click", function() {
        window.open("help.html");
    });*/

    /**************
     * Clean code *
     **************/
    $( ".datepicker" ).datepicker();

    // Init tab navigation
    //changeToTab($(".tabnavi a").first());

    $(".tabNavi a").click(function(){
        changeToTab($(this));
    });
    if(window.location.hash != null && window.location.hash.length > 0) {
        changeToTab($(".tabNavi "+window.location.hash));
    } else {
        $(".tabNavi a").first().click();
    }

    function changeToTab(tab){
        if(!tab.hasClass("selected")){
            $(".tabNavi a").removeClass("selected");
            tab.addClass("selected");
            var selectedId = tab.attr("id");
            $(".tabs").hide();
            $(".tab_" + selectedId).show();
        }
    }

    $(".pointerClass").hover(
        function() {
            $(this).css('cursor', 'pointer');
        },
        function() {
            $(this).css('cursor', 'auto');
        }
    );

    $( "#revisionSearchFormSearch" ).click(function() {
        $( "#revisionSearchForm" ).submit();
    });

    $( "#revisionModifyFormSave" ).click(function() {
        $("#revisionModifyForm").attr("action", MetkaJS.PathBuilder().add(MetkaJS.Globals.page).add("save").build());
        $("#revisionModifyForm").submit();
    });

    $( "#revisionModifyFormApprove" ).click(function() {
        $("#revisionModifyForm").attr("action", MetkaJS.PathBuilder().add(MetkaJS.Globals.page).add("approve").build());
        $("#revisionModifyForm").submit();
    });

    /**
     * Display controller provided errors that are present at page load time.
     */
    if(MetkaJS.ErrorManager != null) MetkaJS.ErrorManager.showAll();
});
