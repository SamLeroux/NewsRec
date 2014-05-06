/* 
 * Copyright 2014 Sam Leroux <sam.leroux@ugent.be>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var backendlessAppId = "0F008386-765A-AF0D-FF36-B411C667FB00";
var backendlessJsId = "D949C164-DF52-D314-FF06-71A543DF2A00";
var backendlessVersion = "V1";

$(document).ready(function() {
    userUtils.init(backendlessAppId, backendlessJsId, backendlessVersion);

    $("#btnRegister").on("click", function() {
        if ($("#confirmPass").is(':visible')) {
            $("#confirmPass").hide();
            $("#btnRegister").text("Register");
        } else {
            $("#confirmPass").show();
            $("#btnRegister").text("Log in");
        }
    });

    $("#submit").on("click", function() {

        if (!($("#pass1") && $("name"))) {
            $().toastmessage('showToast', {
                text: "All fields are required",
                sticky: false,
                type: 'error',
                inEffectDuration: 2500
            });
        }
        if ($("#confirmPass").is(':visible')) {
            var user = $("#name").val();
            var p1 = $("#pass1").val();
            var p2 = $("#pass2").val();

            if (!p2) {
                $().toastmessage('showToast', {
                    text: "All fields are required",
                    sticky: false,
                    type: 'error',
                    inEffectDuration: 2500
                });
            }
            if (p1 === p2) {
                userUtils.register(user, p1);
            } else {
                $().toastmessage('showToast', {
                    text: "Passwords do not match",
                    sticky: false,
                    type: 'error',
                    inEffectDuration: 2500
                });
            }

        }
        else {
            //login
            var user = $("#name").val();
            var pass = $("#pass1").val();
            userUtils.login(user, pass);
        }
    });
    
    
});


var userUtils = {
    init: function(appId, secret, version) {
        Backendless.initApp(appId, secret, version);
    },
    login: function(username, password) {
        console.log(username + " " + password);
        $.mobile.loading('show');
        try {
            userUtils.user = Backendless.UserService.login(username, password);
            userUtils.setUserID(userUtils.user.id);

            localStorage.setItem("loggedInUserName", userUtils.user.username);
            $('#loginDialog').popup('close');
            $().toastmessage('showToast', {
                text: "logged in as " + username,
                sticky: false,
                type: 'notice',
                inEffectDuration: 2000
            });
            start = 0;
            fetchRecommendations(true);
        }
        catch (err) {
            //$('#loginDialog').popup('close');
            var message = err.message;
            if (message === ""){
                message = "Error during login";
            }
            $().toastmessage('showToast', {
                text: message,
                sticky: false,
                type: 'error',
                inEffectDuration: 3500
            });
            console.log("error code - " + err.statusCode);
        }
        $.mobile.loading('hide');
    },
    logout: function() {
        Backendless.UserService.logout();
        localStorage.removeItem("loggedInUserName");
    },
    register: function(u, p1) {
        $.mobile.loading('show');
        var user = new Backendless.User();
        console.log(u + " " + p1);
        user.username = u;
        user.password = p1;
        user.id = userUtils.getUserID();
        Backendless.UserService.register(user, new Backendless.Async(userUtils.userRegistered, userUtils.userRegistrationFailed));
    },
    userRegistered: function(user) {
        $('#loginDialog').popup('close');
        $().toastmessage('showToast', {
            text: "Registration complete",
            sticky: false,
            type: 'notice',
            inEffectDuration: 2500
        });
        var username = $("#name").val();
        var pass = $("#pass1").val();
        userUtils.login(username, pass);
    },
    userRegistrationFailed: function(err) {
        //$('#loginDialog').popup('close');
        $().toastmessage('showToast', {
            text: err.message,
            sticky: false,
            type: 'error',
            inEffectDuration: 3500
        });
        $.mobile.loading('hide');
    },
    getUserID: function() {
        var user = localStorage.getItem("userid");
        if (user === null || !user || user === undefined || user === "undefined") {
            user = $.ajax({
                type: "GET",
                url: "login.do",
                async: false
            }).responseText;
            console.log(user);
            localStorage.setItem("userid", user);
        }
        console.log(user);
        return user;
    },
    setUserID: function(uid) {
        localStorage.setItem("userid", uid);
        user = $.ajax({
            type: "GET",
            url: "login.do?userId=" + uid,
            async: false
        }).responseText;
    }
};
