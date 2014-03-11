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

function getItemDisplayDiv(item) {
    var div = $("<div>");
    div.className = "item";
    var article = $("<article>");

    var title = $("<h1>");
    title.text(item.representative.title + " ("+item.items.length+" members)");
    title.on("click", function (event){
        ratingClick(event,item.representative.id, item.representative.docNr) 
    });

    var timestamp = $("<h2>");
    timestamp.addClass("time");
    timestamp.text(item.representative.timestamp);
    
    /*var source = $("<p>");
    source.className = "source";
    source.text(item.source);
    */
   
    var description = $("<p>");
    description.className = "description";
    var descriptionText = item.representative.description;
    if (description.length > 200){
        descriptionText = descriptionText.substr(0,200)+"...";
    }
    description.text(descriptionText);
    
    article.append(title);
    article.append(timestamp);
    //article.append(source);
    article.append(description);
    
    div.append(article);
    div.append("<hr/>");

    return div;

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
        // Nu getoonde item verwijderden
        if (currentShown === 1) {
            // Nu worden de zoekresultaten getoond
            $("#results").hide();
            $("#message").hide();
            $("#topLeft").hide();
            $("#showMore").hide();
            //$("#results").empty();
        }
        else if (currentShown === 2) {
            // Nu worden de aanbevelingen getoond
            $("#recommended").hide();
            $("#showMore").hide();
            $("#message").hide();
            //$("#results").empty();
        }
        else if (currentShown === 3) {
            // Nu wordt de loginpagina getoond
            $("#login").hide();
        }
        else if (currentShown === 4) {
            // Nu wordt de registreerpagina getoond
            $("#register").hide();
        }

        $(".current").removeClass("current");

        // Wat moet er nu getoond worden ?
        if (what === 1) {
            // zoekpagina tonen
            $("#topLeft").show();
            $("#results").show();
            $("#linkSearch").addClass("current");
            if (showMoreSearchResults) {
                $("#showMore").show();
            }
            else {
                $("#showMore").hide();
            }

        }
        else if (what === 2) {
            // aanbevelingen tonen
            //$("#results").empty();
            $("#recommended").show();
            $("#linkRecommended").addClass("current");
            fetchRecommendations();
        }
        else if (what === 3) {
            // login tonen
            $("#login").show();
            $("#linkLogin").addClass("current");
        }
        else if (what === 4) {
            // registreer formulier tonen
            $("#register").show();
            $("#linkLogin").addClass("current");
        }
        currentShown = what;
    }
}



function fetchRecommendations() {
    $("#loader").show();

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
        console.log(item);
        var div = getItemDisplayDiv(data[item]);
        $("#results").append(div);
    }
    $("#loader").hide();
}

function recommendationsFetchError(xhr, errorType, exception) {
    console.log(xhr);
    $("#loader").hide();
}
