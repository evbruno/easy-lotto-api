
define(

    ['api', 'knockout', 'sammy', 'jquery', 'lotofacil', 'lotofacil-bet', 'home', 'lockr', 'cookies', 'material'],

	function(api, ko, Sammy, $, lotofacilModel, lotofacilBetModel, homeModel, Lockr, Cookies) {

        var MainModel = function () {
            var self = this;

            ko.components.register("home", homeModel);
            ko.components.register("loto", lotofacilModel);
            ko.components.register("loto/bets", lotofacilBetModel);


            this.pageComponent = ko.observable("home");
            this.pageParams = ko.observable();

            this.currentUser = ko.observable();

            this.authUrl = ko.observable();

            $.when(api.getAuthUrl()).done(function(data) {
                self.authUrl(data.url);
                //setTimeout(function(){self.authUrl(data.url)}, 10000);
            });
        };

        var appInit = function() {

            var mainModel = new MainModel();
            ko.applyBindings(mainModel);

            // routes ---------

            var sammy = Sammy('body', function () {});

            var goHome = function() {
                console.log("home");
                loadFromStorage();
                mainModel.pageComponent("home");
                mainModel.pageParams(null);
            };

            var goWelcome = function() {
                console.log("welcome");
                loadFromCookies();
                this.redirect("#/");
            };

            var goLoto = function() {
                console.log("loto");
                loadFromStorage();
                mainModel.pageComponent("loto");
                mainModel.pageParams(null);
            };

            function goComponent(name) {
                return function() {
                    console.log("new component: " + name);
                    mainModel.pageComponent(name);
                    mainModel.pageParams(null);
                }
            }

            sammy.get("#/loto/bets", goComponent("loto/bets"));

            sammy.get("#/loto/bets/:id", function() {
                var _id = this.params['id'];
                console.log("loto bets " + _id);
                mainModel.pageComponent("loto/bets");
                mainModel.pageParams({id: _id});
            });

            var goOut = function() {
                console.log("goOut");
                //loadFromStorage();

                Lockr.flush();
                Cookies.expire('X-EL-UserPictureUrl');
                Cookies.expire('X-EL-UserName');
                Cookies.expire('X-EL-UserEmail');
                Cookies.expire('X-EL-ServerTimestamp');

                mainModel.currentUser(null);

                this.redirect("#/");
            };

            var loadFromStorage = function() {
                console.log("loading from localStorage");
                var user = Lockr.get("user");
                mainModel.currentUser(user);
            };

            var loadFromCookies = function() {
                var picture = Cookies.get("X-EL-UserPictureUrl");
                var name = Cookies.get("X-EL-UserName");
                var email = Cookies.get("X-EL-UserEmail");
                var serverTs = Cookies.get("X-EL-ServerTimestamp");

                Lockr.set("serverTimestamp", serverTs);

                var user = {
                    name: name,
                    email: email,
                    picture: picture
                };

                Lockr.set("user", user);
                mainModel.currentUser(user);

            };

            sammy.get("#/", goHome);
            sammy.get("#/welcome", goWelcome);
            sammy.get("#/loto", goLoto);
            sammy.get("#/out", goOut);

            // init ---------------

            sammy.run("#/");

            $.material.init();
            $.material.ripples();
        };

	    return {
	        boot: appInit
	    }

	}
);