requirejs.config({
    "paths": {
      "jquery":   "//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min",
      "domReady": "//cdnjs.cloudflare.com/ajax/libs/require-domReady/2.0.1/domReady.min"
    }
});

require([ "domReady", "jquery", "ajax_utils", "live_search" ], function(domReady, $, ajaxUtils, liveSearch) {
    // do stuff
    domReady(function() {
        "use strict";
        liveSearch.init();
    });
});