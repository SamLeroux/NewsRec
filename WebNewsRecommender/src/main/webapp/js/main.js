var start = 0;
var count = 500;
var canFetch = true;
var clickInArticle = true;

$(document).ready(function() {
    // Zorg ervoor dat de gebruiker kan zoeken door op enter te drukken in het zoekveld
    $('#queryinput').keypress(function(e) {
        if (e.which === 10 || e.which === 13) {
            btnClicked();
        }
    });


    $("#articleFrame").on("load", function() {
        articleFrameSourceChanged();
    });

    $("#btnBack").on("click", function() {
        btnBackClicked();
    });

    $("#btnRefresh").on("click", function() {
        fetchRecommendations();
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
    }

    fetchRecommendations();

//    $(window).scroll(function() {
//        if ($(window).scrollTop() + $(window).height() === $(document).height()) {
//            if (canFetch) {
//                count += 250;
//                fetchRecommendations();
//            }
//        }
//    });
    window.scrollTo(0, 0);
});

function isPhone() {
    return window.matchMedia("screen and (max-width: 700px)").matches;
}

function getItemDisplayLi(item) {
    var li = $("<li>");


    var a = $("<a>");
    a.on("click", function(event) {
        clickInArticle = false;
        displayArticle(item.representative.url);
        ratingClick(event, item.representative.id, item.representative.docNr);
    });

    var p = $("<p>");
    p.addClass("iconWrapper");
    if (item.representative.imageUrl) {
        var img = $("<img>");
        img.attr("src", item.representative.imageUrl);
        p.append(img);
        a.append(p);
    }
    var h1 = $("<h2>");
    h1.addClass("title");
    var title = item.representative.title;
    h1.html(shorten(title, 100));



    if (item.representative.recommendedBy === "personal") {
        h1.css("color", "red");
    }
    else if (item.representative.recommendedBy === "trending") {
        h1.css("color", "green");
    }
    a.append(h1);

    var h3 = $("<p>");
    h3.addClass("timestamp");
    h3.addClass("ui-li-aside");
    var d = new Date(item.representative.timestamp);
    h3.text(toDateString(d));
    h1.append(h3);

    //var p = $("<p>");

    //p.text(item.representative.description);
    //a.append(p);

    var span = $("<span>");
    span.addClass("ui-li-count");
    span.html(item.items.length + 1);
    a.append(span);

    li.append(a);

    console.log(item.representative.title + " : " + item.items.length + " members");
    return li;

}

function displayArticle(url) {
    console.log(url);

    $.mobile.showPageLoadingMsg();
    if (isPhone()) {
        $("#resultsDiv").hide();
        $("#articleDiv").show();
        $("#btnBack").show();
        canFetch = false;
    }

    var iframe = $("#articleFrame");
    iframe.attr("src", url);

    $("#articleDiv").height($("#resultsDiv").height());

    iframe.show();
    iframe.load(function() {
        $.mobile.hidePageLoadingMsg();
        window.scrollTo(0, 0);
    });
    iframe.error(function() {
        $.mobile.hidePageLoadingMsg();
    });

}


function ratingClick(event, id, docNr) {
    event.stopPropagation();
    $.ajax({
        type: 'POST',
        url: "view.do?itemId=" + id + "&docNr=" + docNr,
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
    $.mobile.showPageLoadingMsg();
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
        var li = getItemDisplayLi(data[item]);
        $("#results").append(li);
    }
    $("#results").listview("refresh");
    //window.scrollTo(0, 0);
    $.mobile.hidePageLoadingMsg();
    canFetch = true;

}

function recommendationsFetchError(xhr, errorType, exception) {
    console.log(xhr);
    $.mobile.hidePageLoadingMsg();
    canFetch = true;
}



function btnBackClicked() {
    $("#resultsDiv").show();
    $("#articleDiv").hide();
    $("#btnBack").hide();
    canFetch = true;
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

function articleFrameSourceChanged(event){
    if (clickInArticle){
        var frame = $("#articleFrame");
        urlViewed(frame.attr("src"));
    }
    else{
        clickInArticle = true;
    }
}
