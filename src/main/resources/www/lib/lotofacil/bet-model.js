define(['text!../lib/lotofacil/bet-template.html', 'knockout', 'underscore', 'api', 'natural-sort'],
    function (template, ko, _, api, naturalSort) {

        var FormBet = function(vals) {
            this.values = vals.sort(naturalSort());

            this.toString = function() {
                return this.values.join(" ");
            };
        };

        var BetViewModel = function (params) {
            var self = this;
            var MAX = 15;
            var MIN = MAX;

            self.currentBet = ko.observable();
            self.currentValues = ko.observableArray();
            self.currentEmails = ko.observableArray();
            self.currentValuesMatrix = ko.observableArray();
            self.errorMessage = ko.observable();
            self.betEmail = ko.observable();

            self.drawBegin = ko.observable().extend({required: true, max: 2000});

            self.addBetValue = function () {
                var n = parseInt(this);

                if (!self.isSelected(n) && self.currentValues().length >= MAX) {
                    self.errorMessage("Máximo de " + MAX + " apostas atingidas.");
                    return;
                }

                self.errorMessage(null);

                if (!self.isSelected(n))
                    self.currentValues.push(n);
                else
                    self.currentValues.remove(n);
            };

            self.addValueFromMatrix = function() {
                self.currentValues(this.values);
                self.currentValuesMatrix.remove(this);
            };

            self.removeValueFromMatrix = function() {
                self.currentValuesMatrix.remove(this);
            };

            self.addEmail = function() {
                self.currentEmails.push(self.betEmail().toString());
                self.betEmail(null);
            };

            self.removeEmail = function() {
                self.currentEmails.remove(this.toString());
                self.betEmail(null);
            };

            self.updateEmail = function() {
                self.betEmail(this.toString());
                self.currentEmails.remove(this.toString());
            };

            self.addEmailOnEnter = function() {
                var ENTER_KEY = 13;
                if (arguments[1].keyCode === ENTER_KEY) {
                    self.addEmail();
                    return false;
                }

                return true;
            };

            self.remainingValues = ko.computed(function() {
                var n = Math.max(0, MAX - self.currentValues().length);
                if (!n) return "";
                return n + "/" + MAX;
            });

            self.canAddValuesToTheMatrix = ko.pureComputed(function() {
                return self.currentValues().length <= MAX &&
                        self.currentValues().length >= MIN;
            });

            self.addBetValueInMatrix = function () {
                self.errorMessage(null);
                var curr = self.currentValues.removeAll();
                var obj = new FormBet(curr);
                self.currentValuesMatrix.push(obj);
            };

            self.isSelected = function(n) {
                return self.currentValues.indexOf(parseInt(n)) >= 0;
            };

            self.resetForm = function() {
                self.currentValues([]);
                self.currentBet(null);
                return true;
            };

            if (params && params.id) {
                console.log("Current bet: " + params.id);
                self.currentBet(params.id);
            } else {
                console.log("Current bet empty... ");
            }

            self.lotofacilGrid = ko.pureComputed(function () {
                var r = [];
                for (var i = 1; i <= 5; i++) {
                    var s = [];
                    for (var j = 1; j <= 5; j++)
                        s.push((i - 1) * 5 + j);
                    r.push(s);
                }
                return r;
            });

            self.save = function() {
                //var errors = ko.validation.group([self.drawBegin]);
                console.log(this);
                return false;
            };
        };

        return {
            viewModel: BetViewModel,
            template: template
        };

    }
);