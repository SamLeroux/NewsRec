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
    var div = document.createElement("div");
    div.className = 'search_item';
    div.onclick = function(event) {
        ratingClick(event, item.id, item.docNr);
        return false;
    };

    var table = document.createElement("table");
    table.width = '100%';
    div.appendChild(table);

    var tr = document.createElement("tr");
    table.appendChild(tr);

    var tdImage = document.createElement("td");
    tdImage.vAlign = 'top';
    tdImage.align = 'left';
    tr.appendChild(tdImage);

    var img = document.createElement("img");
    img.width = 120;
    img.height = 80;
    img.src = "img/nia.jpeg";
    tdImage.appendChild(img);

    var tdDescription = document.createElement("td");
    tdDescription.vAlign = 'top';
    tdDescription.align = 'left';
    tdDescription.width = '100%';
    tr.appendChild(tdDescription);


    var description = "";
    description += "<b>" + item.title + "</b><br/>";
    description += "<i>" + item.timestamp + "</i></br>";

    description += "<span class='videoDescription'>" + item.description + "</span><br/>";
    description += "<br/><table><tr>";



    description += '</div></td>';

    description += "<tr></table>";

    tdDescription.innerHTML = description;

    // http://viralpatel.net/blogs/dynamically-shortened-text-show-more-link-jquery/
    $(tdDescription).shorten({
        "showChars": 200
    });

    // Horizontale lijn onderaan
    var hr = document.createElement("hr");
    div.appendChild(hr);

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
        url: "GetRecommendations.do?count=10&start=0",
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
