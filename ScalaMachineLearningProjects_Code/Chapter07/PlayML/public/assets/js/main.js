angular.module("App", ['chart.js']).controller("Ctrl", ['$scope', '$http', function ($scope, $http) {
    $scope.isLoading = false;
    $scope.form = {
        REWARD_TYPE: "Maximum reward",
        NUM_NEIGHBOR_STATES: 3,
        STRIKE_PRICE: 190.0,
        MIN_TIME_EXPIRATION: 6,
        QUANTIZATION_STEP: 32,
        ALPHA: 0.2,
        DISCOUNT: 0.6,
        MAX_EPISODE_LEN: 128,
        NUM_EPISODES: 20,
        MIN_COVERAGE: 0.0
    };
    $scope.run = function () {

        $scope.isLoading = true;
        $scope.result = null;

        var formData = new FormData(document.getElementById('form'));
        $http.post('/api/compute', formData, {
            headers: {'Content-Type': undefined}
        }).then(function successCallback(response) {
            $scope.isLoading = false;

            if (response.data.exception) {
                $scope.error = response.data.exception;
                $('#modal1').openModal();
                return;
            }
            $scope.result = response.data;

            $('#canvasContainer').html('');

            if (response.data.OPTIMAL) {
                $('#canvasContainer').append('<canvas id="optimalCanvas"></canvas>')
                Chart.Scatter(document.getElementById("optimalCanvas").getContext("2d"), {
                    data: {
                        datasets: [{
                            data: response.data.OPTIMAL
                        }]
                    }, options: {
                        title: {
                            display: true,
                            text: 'Optimal'
                        }
                    }
                });
            }

            if (response.data.Q_VALUE) {
                $('#canvasContainer').append('<canvas id="valuesCanvas"></canvas>')

                Chart.Line(document.getElementById("valuesCanvas").getContext("2d"), {
                    data: {
                        labels: new Array(response.data.Q_VALUE.length),
                        datasets: [{
                            data: response.data.Q_VALUE
                        }]
                    }, options: {
                        title: {
                            display: true,
                            text: 'Q-Value'
                        }
                    }

                });
            }

        }, function errorCallback(error) {
            $scope.isLoading = false;
            $scope.error = error.statusText == "" ? 'Cant reach the server' : error.statusText;
            $('#modal1').openModal();
        });


    }
}]);


$(document).ready(function () {
    $('.slider').slider();
    $('.parallax').parallax();
    $('.scrollspy').scrollSpy();
    $(".dropdown-button").dropdown();
    $(".button-collapse").sideNav();
    $('select').material_select();
    $('.datepicker').pickadate({
        selectMonths: true,
        selectYears: 15
    });
    $('.modal-trigger').leanModal();
    $('.carousel').carousel();
});

