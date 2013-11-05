define(['jquery', 'ajax_utils'], function($, ajaxUtils) {

    "use strict";

    var queryStringToObject = function() {
        var pairs = location.search.slice(1).split('&');
        var result = {};
        pairs.forEach(function(pair) {
            pair = pair.split('=');
            result[pair[0]] = decodeURIComponent(pair[1] || '');
        });
        return result;
    };
    
    var queryFromUrl = function() {
        return queryStringToObject().query;
    };
    
    var sortByFromUrl = function() {
        return queryStringToObject().sortBy;
    };
    
    var secondsAgo = function(date) {
        return (new Date().getTime() - date.getTime()) / 1000;
    };

    var lastQueryFinishedAt = null;
    var updateHistoryForQuery = function(q, s) {
        if (history.pushState) {
            var newUrl = jsRoutes.controllers.ProductsController.search(q, s).url;
            // For searches withing 5 seconds we don't want to create new history entries
            if(!lastQueryFinishedAt
                    || lastQueryFinishedAt && secondsAgo(lastQueryFinishedAt) < 5)
                history.replaceState(null, null, newUrl);
            else
                history.pushState(null, null, newUrl);
            lastQueryFinishedAt = new Date();
        }
    };
    
    var setWindowTitleForQuery = function(q) {
        if(!q) {
            return;
        }
        var title = window.document.title;
        var idx = title.indexOf("(");
        window.document.title = idx == -1
            ? title + " (" + q + ")"
            : title.substring(0, idx) + "(" + q + ")";
    };
    
    var lastQuery = null;
    var lastSortBy = null;

    window.addEventListener("popstate", function(event) {
        // Only trigger live search if there ran any ajax livesearch before,
        // or if the user navigated forward/backward after loading the page
        var q = queryFromUrl();
        var s = sortByFromUrl();
        if(lastQuery || q != $("#searchbox").val() || s != $("#sortBy").val()) {
            $("#searchbox").val(q);
            $("#sortBy").val(s);
            liveSearch(null, true);
        }
    });

    var liveSearch = function liveSearch(event, isPopstate) {
        var q = $('#searchbox').val(),
            s = $('#sortBy').val();
        
        // Skip ajax request if the query didn't change (e.g. because the keyup
        // event was caused be cursor/Pos1/End keys or because chars were added
        // and removed again)
        if(q == queryFromUrl() && s == sortByFromUrl() && !isPopstate || q == lastQuery && s == lastSortBy) {
            return;
        }

        lastQuery = q;
        lastSortBy = s;
        $("#loading-message").text("Searching " + q);
        $("#overlay").fadeIn(100);
        console.log("running liveSearch with " + q);
        jsRoutes.controllers.ProductsController.liveSearch(q, s).ajax({
            success : function(data, textStatus, jqXHR) {
                // If there was another query started we don't take any action
                if(lastQuery != q || lastSortBy != s) {
                    return;
                }

                console.log("Got response for query " + q);

                // Update the history if we didn't get invoked by a history event
                if(!isPopstate)
                    updateHistoryForQuery(q, s);

                // Update window/page
                setWindowTitleForQuery(q);
                ajaxUtils.updateHtmlBySelector(data);

                $("#overlay").fadeOut(500);
            },
            dataType : "json"
        });
    };

    /**
     * Debouncing ensures that exactly one signal is sent for an event that may be happening several times. As long as the events are occurring fast enough to
     * happen at least once in every detection period, the signal will not be sent!
     * 
     * http://unscriptable.com/2009/03/20/debouncing-javascript-methods/
     * 
     * @param func
     *                the function to invoke
     * @param threshold
     *                the detection period
     * @param execAsap
     *                a boolean indicating whether the signal should happen at the beginning of the detection period (true) or the end.
     */
    var debounce = function(func, threshold, execAsap) {
        
        // console.log("debounce called with func ", func);

        // handle to setTimeout async task (detection period)
        var timeout;

        // return the new debounced function which executes 'func' only once
        // until the detection period expires
        return function debounced() {
            // reference to original context object
            var obj = this;
            // arguments at execution time
            var args = arguments;
            // this is the detection function. it will be executed if/when the threshold expires
            function delayed() {
                // if we're executing at the end of the detection period
                if (!execAsap) {
                    // execute now
                    func.apply(obj, args);
                }
                // clear timeout handle
                timeout = null;
            };

            // stop any current detection period
            if (timeout)
                clearTimeout(timeout);
            // otherwise, if we're not already waiting and we're executing at the beginning of the detection period
            else if (execAsap)
                func.apply(obj, args);

            // reset the detection period
            timeout = setTimeout(delayed, threshold || 100);
        };

    };
    
    var init = function init() {
        $("#overlay").hide();
        $("#searchbox").keyup(debounce(liveSearch, 300, false));
        $("#sortBy").on("change", liveSearch);
        setWindowTitleForQuery(queryFromUrl());
        // Change the url instead of submitting the form, because the form submit
        // would encode spaces as "+" which would not be decoded as space by decodeURIComponent
        $("#searchsubmit").click(function() {
            var query  = $('#searchbox').val(),
                sortBy = $('#sortBy').val();
            location.href = jsRoutes.controllers.ProductsController.search(query, sortBy).url;
            return false;
        });
    };

    return {
        init: init
    }

});