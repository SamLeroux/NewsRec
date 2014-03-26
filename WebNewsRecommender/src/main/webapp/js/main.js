$(function() {
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
    
    $("#btnRefresh").on("click", function(){
        fetchRecommendations();
    });

    $("#results").listview();
    fetchRecommendations();
    window.scrollTo(0, 0);
});

function isPhone() {
    return window.matchMedia("screen and (max-width: 700px)").matches;
}

function getItemDisplayLi(item) {
    var li = $("<li>");


    var a = $("<a>");
    a.on("click", function(event) {
        displayArticle(item.representative.url);
        ratingClick(event, item.representative.id, item.representative.docNr);
    });

    var h1 = $("<h1>");
    h1.addClass("myHeader");
    h1.text(item.representative.title);
    if (item.representative.recommendedBy === "personal") {
        h1.css("color", "red");
    }
    else if (item.representative.recommendedBy === "trending") {
        h1.css("color", "green");
    }
    a.append(h1);

    var h3 = $("<h3>");
    h3.addClass("timestamp");
    h3.text(item.representative.timestamp + "  (" + item.items.length + " members)");
    a.append(h3);

    var p = $("<p>");
    p.addClass("myParagraph");
    p.text(item.representative.description);
    a.append(p);

    li.append(a);
    return li;

}

function displayArticle(url) {
    console.log(url);
    $.mobile.showPageLoadingMsg();
    if (isPhone()) {
        $("#resultsDiv").hide();
        $("#articleDiv").show();
        $("#btnBack").show();
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

function fetchRecommendations() {
    $.mobile.showPageLoadingMsg();

    $.ajax({
        url: "GetRecommendations.do?count=250&start=0",
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
    window.scrollTo(0, 0);
    $.mobile.hidePageLoadingMsg();

}

function recommendationsFetchError(xhr, errorType, exception) {
    console.log(xhr);
    $.mobile.hidePageLoadingMsg();
}


function articleFrameSourceChanged() {
    console.log($("#articleFrame").attr("src"));
}



function dispatchEvent(event) {
    console.log(event.animationName);
    isPhone = (event.animationName === "phone");
}

function btnBackClicked() {
    $("#resultsDiv").show();
    $("#articleDiv").hide();
    $("#btnBack").hide();
}