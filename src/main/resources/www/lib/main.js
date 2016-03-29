requirejs.config(
    {
        baseUrl: 'lib',
        shim: {
            bootstrap: {
                deps: ["jquery"],
                exports: 'jquery'
            },
            ripples: {
                deps: ["jquery"]
            },
            material: {
                deps: ["bootstrap", 'ripples', 'arrive']
            },
            arrive: {
                deps: ['jquery']
            },
            'natural-sort': {
                exports: 'naturalSort'
            }
        },
        paths: {
            //app: '../app'
            // 3rd
            'text': 'vendor/requirejs-text/text',
            'knockout': 'vendor/knockout/dist/knockout',
            //'knockout-validation': 'vendor/knockout-validation/dist/knockout.validation.min',
            'sammy': 'vendor/sammy/lib/sammy',
            'jquery': 'vendor/jquery/dist/jquery.min',
            'bootstrap': 'vendor/bootstrap/dist/js/bootstrap',
            'material': 'vendor/bootstrap-material-design/dist/js/material',
            'ripples': 'vendor/bootstrap-material-design/dist/js/ripples',
            'arrive': 'vendor/arrive/src/arrive',
            'underscore': 'vendor/underscore/underscore-min',
            'lockr': 'vendor/lockr/lockr.min',
            'cookies': 'vendor/cookies-js/dist/cookies.min',
            'natural-sort': 'vendor/natural-sort/dist/natural-sort',
            // mine
            'app': 'app',
            'api': 'loto-api',
            'lotofacil': 'lotofacil/loto-model',
            'lotofacil-bet': 'lotofacil/bet-model',
            'home': 'home/home-model'

        }
    });

requirejs(
    ['app'],
    function (app) {
        app.boot();
    }
);