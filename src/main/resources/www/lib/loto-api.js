define(
	['jquery'],

	function ($) {

        var URL_API_PREFIX = ( window.location.origin === "http://127.0.0.1:9001" || window.location.origin === "http://lvh.me:9001" )
                            ? "http://127.0.0.1:9000"
                            : window.location.origin;

        var count = 0;

        return {
            getLotofacil: function() {
                console.log("API calls: " + count);
                return $.getJSON(URL_API_PREFIX + "/api/lotofacil");
            },

            getLotofacilBets: function(concurso) {
                console.log("API calls: " + ++count);
                return $.getJSON(URL_API_PREFIX + "/api/lotofacil/" + concurso + "/bets");
            },

            getAuthUrl: function(provider) {
                provider = provider || "google";
                console.log("API calls: " + ++count);
                return $.getJSON(URL_API_PREFIX + "/auth/" + provider + "/url");
            },

            saveBet: function(bet) {
                console.log("API calls: " + ++count);

                var result = $.Deferred();
                var url = URL_API_PREFIX + "/api/lotofacil/bets";
                var options = {
                    url: url,
                    type: 'POST',
                    data: JSON.stringify(bet),
                    contentType : 'application/json; charset=utf-8',
                    dataType: 'json'
                };

                $.ajax(options).done(function (data, status, xhr) {
                    result.resolve(data);
                }).always(function(xhr){
                    console.log("Always: " + xhr);
                }).fail(function (xhr, textStatus, error) {
                    console.error("Fail [" + xhr.status + "] : " + xhr.responseText + " / " + textStatus  + " / " + error);
                });

                return result.promise();
            }
        }

});



