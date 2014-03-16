$(function() {
    // Zorg ervoor dat de gebruiker kan zoeken door op enter te drukken in het zoekveld
    $('#queryinput').keypress(function(e) {
        if (e.which === 10 || e.which === 13) {
            btnClicked();
        }
    });

    fetchRecommendations();
    // Volledig naar boven scrollen
    window.scrollTo(0, 0);


});

function btnClicked() {
    fetchRecommendations();
    window.scrollTo(0, 0);
}

function getItemDisplayLi(item) {
    var li = $("<li>");
    var a = $("<a>");
    a.on("click", function() {
        displayArticle(item.representative.url);
    });
    var h1 = $("<h1>");
    h1.addClass("myHeader");
    h1.text(item.representative.title);
    a.append(h1);

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
    var iframe = $("#articleFrame");
    iframe.attr("src", url);
    iframe.height = iframe.contents().height();
    $("#contentDiv").height($("#resultsDiv").height());
    $("#contentDiv").show();
    iframe.load(function() {
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

function display(what) {
    if (currentShown !== what) {

        currentShown = what;
    }
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
    console.log(data);
    for (var item in data) {
        var li = getItemDisplayLi(data[item]);
        $("#results").append(li);
    }
    $("#loader").hide();

    $("#resultsDiv").show();
    $("#results").listview("refresh");
    $.mobile.hidePageLoadingMsg();
}

function recommendationsFetchError(xhr, errorType, exception) {
    console.log(xhr);
}
