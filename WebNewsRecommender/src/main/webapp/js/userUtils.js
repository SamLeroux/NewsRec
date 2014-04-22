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
        if ($("#confirmPass").is(':visible')) {
            var user = $("#name").val();
            var p1 = $("#pass1").val();
            var p2 = $("#pass2").val();
            if (p1 === p2) {
                userUtils.register(user, pass);
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
    user: null,
    init: function(appId, secret, version) {
        Backendless.initApp(appId, secret, version);
    },
    login: function(username, password) {
        showErrorMessage("hello", 1000);
        try {
            userUtils.user = Backendless.UserService.login(username, password);
            userUtils.setUserID(userUtils.user.uuid);

            localStorage.setItem("loggedInUserName", userUtils.user.username);
        }
        catch (err) {
            showErrorMessage(err.message, 20000);
            console.log("error code - " + err.statusCode);
        }
    },
    logout: function() {
        Backendless.UserService.logout();
        localStorage.removeItem("loggedInUserName");
    },
    register: function(u, p1) {
        var user = new Backendless.User();
        user.password = p1;
        user.username = u;
        Backendless.UserService.register(user, new Backendless.Async(userUtils.userRegistered, userUtils.userRegistrationFailed));
    },
    userRegistered: function(user) {
        $.alert("registration complete");
    },
    userRegistrationFailed: function(err) {
        $.alert(err.message);
    },
    getUserID: function() {
        var user = localStorage.getItem("userid");
        return user;
    },
    setUserID: function(uid) {
        localStorage.setItem("userid", uid);
    }
};
