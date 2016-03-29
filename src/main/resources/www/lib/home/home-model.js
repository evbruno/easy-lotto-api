define(['text!../lib/home/home-template.html','knockout'], function(template, ko) {

    var HomeViewModel = function(params) {
        var self = this;
    };

    return {
        viewModel: HomeViewModel,
        template: template
    };
});