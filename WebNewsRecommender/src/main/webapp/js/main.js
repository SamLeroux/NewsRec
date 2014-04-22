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

var start = 0;
var count = 250;

var canFetch = true;
var clickInArticle = true;

var lastResults = "AllResults"; // "AllResults" or "RelatedResults" 

$(document).ready(function() {

    $("#btnBack").on("click", function() {
        btnBackClicked();
    });

    $("#btnRefresh").on("click", function() {
        fetchRecommendations();
    });

    $("#btnLogin").on("click", function() {
        btnLoginClicked();
    });

    $("#results").listview();

    if (isPhone()) {
        $("#btnRefresh").addClass("ui-btn-icon-notext");
        $("#btnRefresh").addClass("ui-corner-all");
        $("#btnRefresh").removeClass("ui-btn-icon-left");
        $("#btnRefresh").removeClass("ui-shadow");

        $("#btnBack").addClass("ui-btn-icon-notext");
        $("#btnBack").addClass("ui-corner-all");
        $("#btnBack").removeClass("ui-btn-icon-left");
        $("#btnBack").removeClass("ui-shadow");

        $("#btnLogin").addClass("ui-btn-icon-notext");
        $("#btnLogin").addClass("ui-corner-all");
        $("#btnLogin").removeClass("ui-btn-icon-left");
        $("#btnLogin").removeClass("ui-shadow");
    }

    fetchRecommendations();

    $("#resultsDiv").scroll(function() {
        if ($("#resultsDiv").scrollTop() +
                $("#resultsDiv").innerHeight()
                >= $("#resultsDiv")[0].scrollHeight) {
            if (canFetch) {
                count += 250;
                fetchRecommendations();
            }
        }
    });
    window.scrollTo(0, 0);
});

function isPhone() {
    return window.matchMedia("screen and (max-width: 700px)").matches;
}

function getClusterDisplayLi(cluster) {
    var li = $("<li>");


    var a = $("<a>");
    a.on("click", function(event) {
        clickInArticle = false;
        displayArticle(cluster.representative.url);
        ratingClick(event, cluster.representative.id);
    });

    var p = $("<p>");
    p.addClass("iconWrapper");
    if (cluster.representative.imageUrl) {
        var img = $("<img>");
        img.attr("src", cluster.representative.imageUrl);
        p.append(img);
        a.append(p);
    }
    var h1 = $("<h2>");
    h1.addClass("title");
    var title = cluster.representative.title;
    h1.html(shorten(title, 100));



//    if (cluster.representative.recommendedBy === "personal") {
//        h1.css("color", "red");
//    }
//    else if (cluster.representative.recommendedBy === "trending") {
//        h1.css("color", "green");
//    }
    a.append(h1);

    var h3 = $("<p>");
    h3.addClass("timestamp");
    h3.addClass("ui-li-aside");
    var d = new Date(cluster.representative.timestamp);
    h3.text(toDateString(d));
    h1.append(h3);

    //var p = $("<p>");

    //p.text(item.representative.description);
    //a.append(p);

    if (cluster.items.length > 0) {
        var span = $("<a>");
        span.addClass("ui-li-count");
        span.on("click", function(event) {
            event.stopPropagation();
            lastResults = "AllResults";
            canFetch = false;

            $("#relatedResults").empty();
            $("#btnRefresh").hide();
            for (var i in cluster.items) {
                var li = getItemDisplayLi(cluster.items[i]);
                $("#relatedResults").append(li);
            }

            $("#relatedResults").listview("refresh");
            $("#resultsDiv").hide();
            $("#relatedResultsDiv").scrollTop(0);
            $("#relatedResultsDiv").show();
            $("#btnBack").show();
        });
        span.html(cluster.items.length);
        a.append(span);


    }

    li.append(a);

    console.log(cluster.representative.title + " : " + cluster.items.length + " members");
    return li;


}


function getItemDisplayLi(item) {
    var li = $("<li>");


    var a = $("<a>");


    a.on("click", function(event) {
        lastResults = "RelatedResults";
        clickInArticle = false;
        displayArticle(item.url);
        ratingClick(event, item.id);
    });

    var p = $("<p>");
    p.addClass("iconWrapper");
    if (item.imageUrl) {
        var img = $("<img>");
        img.attr("src", item.imageUrl);
        p.append(img);
        a.append(p);
    }
    var h1 = $("<h2>");
    h1.addClass("title");
    var title = item.title;
    h1.html(shorten(title, 100));



//    if (item.recommendedBy === "personal") {
//        h1.css("color", "red");
//    }
//    else if (item.recommendedBy === "trending") {
//        h1.css("color", "green");
//    }
    a.append(h1);

    var h3 = $("<p>");
    h3.addClass("timestamp");
    h3.addClass("ui-li-aside");
    var d = new Date(item.timestamp);
    h3.text(toDateString(d));
    h1.append(h3);

    //var p = $("<p>");

    //p.text(item.representative.description);
    //a.append(p);

    li.append(a);

    return li;

}
function displayArticle(url) {
    $.mobile.loading('show');
    if (isPhone()) {
        $("#resultsDiv").hide();
        $("#articleDiv").show();
        $("#btnBack").show();
        canFetch = false;
    }

    var iframe = $("#articleFrame");
    iframe.attr("src", url);
    iframe.show();

    iframe.load(function() {
        console.log("iframe source changed 2: " + iframe.attr("src"));
        $.mobile.loading('hide');
        window.scrollTo(0, 0);
    });
    iframe.error(function() {
        $.mobile.loading('hide');
    });

}


function ratingClick(event, id) {
    event.stopPropagation();
    $.ajax({
        type: 'POST',
        url: "view.do?itemId=" + id,
        dataType: "json",
        success: function() {
            console.log("recorded view");
        },
        error: function() {
            console.log("error recording view");
        }
    });

}

function urlViewed(url) {
    $.ajax({
        type: 'POST',
        url: "view.do?url=" + url,
        dataType: "json",
        success: function() {
            console.log("recorded view");
        },
        error: function() {
            console.log("error recording view");
        }
    });

}

function fetchRecommendations() {
    $.mobile.loading('show');
    canFetch = false;
    $.ajax({
        url: "GetRecommendations.do?count=" + count + "&start=" + start,
        dataType: "json",
        success: recommendationsFetched,
        error: recommendationsFetchError
    });

}

function recommendationsFetched(data) {
    $("#results").empty();
    for (var item in data) {
        var li = getClusterDisplayLi(data[item]);
        $("#results").append(li);
    }
    $("#results").listview("refresh");
    $.mobile.loading('hide');
    canFetch = true;

}

function recommendationsFetchError(xhr, errorType, exception) {
    console.log(xhr);
    $.mobile.loading('hide');
    canFetch = true;
}



function btnBackClicked() {
    $.mobile.loading('hide');
    if (lastResults === "AllResults") {
        $("#resultsDiv").show();
        $("#btnBack").hide();
        $("#btnRefresh").show();
        $("#relatedResultsDiv").hide();
        canFetch = true;
        if (isPhone()) {
            $("#articleDiv").hide();
        }
    }
    else if (lastResults === "RelatedResults") {
        $("#resultsDiv").hide();
        $("#relatedResultsDiv").show();
        lastResults = "AllResults";
        canFetch = false;
        if (isPhone()) {
            $("#articleDiv").hide();
        }
    }

}

function toDateString(date) {
    var day = date.getDate();
    var month = date.getMonth();
    var year = date.getYear();

    var today = new Date();

    if (today.getDate() === day && today.getMonth() === month && today.getYear() === year) {
        var age = today.getHours() - date.getHours();
        var suffix = " hours ago";

        if (age === 0) {
            age = today.getMinutes() - date.getMinutes();
            suffix = " minutes ago";
        }
        else if (age === 1) {
            suffix = " hour ago";
        }
        return age + suffix;
    }
    else if (today.getDate() === day + 1 && today.getMonth() === month && today.getYear() === year) {
        return "Yesterday";
    }
    else if (today.getDate() <= day + 7 && today.getMonth() === month && today.getYear() === year) {
        return today.getDate() - day + " days ago";
    }
    else {
        return date.toLocaleString();
    }
}

function shorten(text, n) {
    var result = text;
    if (text.length > n) {
        var pos = text.substring(0, n - 3).lastIndexOf(" ");
        console.log(pos);
        if (pos > 0) {
            result = text.substring(0, pos) + " ...";
        }
        else {
            result = text.substring(0, n - 1) + " ...";
        }
    }
    return result;

}


function btnLoginClicked() {
    console.log("loginclicked");
    $('#loginDialog').popup('open');
}