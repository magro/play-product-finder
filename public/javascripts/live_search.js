window.myApp = window.myApp || {};

window.myApp.AjaxInput = (function() {
    "use strict";
    
    var queryFromUrl = function() {
        var loc = window.location.href;
        var idx = loc.indexOf("query=");
        if(idx > -1) {
            return decodeURIComponent(loc.substring(idx + "query=".length));
        }
        return null;
    };
    
    var urlWithQuery = function(q) {
        var loc = window.location.href;
        var idx = loc.indexOf("query=");
        var qEnc = encodeURIComponent(q);
        return idx == -1 ? loc + "?query=" + qEnc : loc.substring(0, idx) + "query=" + qEnc;
    };
    
    var secondsAgo = function(date) {
        return (new Date().getTime() - date.getTime()) / 1000;
    };

    var lastQueryFinishedAt = null;
    var updateHistoryForQuery = function(q) {
        if (history.pushState) {
            var newUrl = urlWithQuery(q);
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
    setWindowTitleForQuery(queryFromUrl());
    
    var lastQuery = null;
    
    window.addEventListener("popstate", function(event) {
        // Only trigger live search if there ran any ajax livesearch before,
        // or if the user navigated forward/backward after loading the page
        var q = queryFromUrl();
        if(lastQuery || q != $("#searchbox").val()) {
            $("#searchbox").val(q);
            liveSearch(null, true);
        }
    });

    var liveSearch = function liveSearch(event, isPopstate) {
        var q = $("#searchbox").val();
        
        // Skip ajax request if the query didn't change (e.g. because the keyup
        // event was caused be cursor/Pos1/End keys or because chars were added
        // and removed again)
        if(q == queryFromUrl() && !isPopstate || q == lastQuery) {
            return;
        }

        console.log("running liveSearch with " + q);
        // var sortBy = $(".computers").attr("data-sort-by");
        // var sortOrder = $(".computers").attr("data-sort-order");
        lastQuery = q;
        jsRoutes.controllers.ProductsController.liveSearch(q).ajax({
            success : function(data, textStatus, jqXHR) {
                // If there was another query started we don't take any action
                if(lastQuery != q) {
                    return;
                }

                console.log("Got response for query " + q);
                if(!isPopstate)
                    updateHistoryForQuery(q);
                setWindowTitleForQuery(q);
                // $(document).off('keyup', '#searchbox');
                window.ajaxUtils.updateHtmlBySelector(data);
                // $(document).on('keyup', '#searchbox', debounce(liveSearch, 300, false));
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
        $("#searchbox").keyup(debounce(liveSearch, 300, false));
    };

    return {
        init : init
    };
})();