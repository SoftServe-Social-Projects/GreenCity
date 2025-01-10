function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}
function clearAllTagsInTagList() {
    document.getElementById("tagsEdit").innerHTML='';
}

function changeIcons() {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    var fieldIcons = {
        'id': 'id-icon',
        'organizer': 'organizer-icon',
        'title': 'title-icon',
        'description': 'description-icon',
        'tags': 'tags-icon'
    };

    if (sort !== null) {
        var [field, direction] = sort.split(',');
        var iconId = fieldIcons[field];

        if (iconId) {
            var iconElement = document.getElementById(iconId);
            if (iconElement) {
                iconElement.className = direction === 'ASC' ? 'fas fa-chevron-up text-success' : 'fas fa-chevron-down text-success';
            }
        }
    }
}


function sortByFieldName(fieldName) {
    var currentParams = new URLSearchParams(window.location.search);
    var currentSort = currentParams.get("sort");

    if (currentSort && currentSort.includes(fieldName)) {
        if (currentSort.includes('ASC')) {
            currentParams.set("sort", fieldName + ',DESC');
        } else {
            currentParams.set("sort", fieldName + ',ASC');
        }
    } else {
        currentParams.set("sort", fieldName + ',ASC');
    }

    window.location.search = currentParams.toString();
}



function sendDataFromSearchForm() {
    let url = "/management/events?";
    let id = document.getElementById("idSearchForm").value;
    let author = document.getElementById("authorSearchForm").value;
    let title = document.getElementById("titleSearchForm").value;
    let text = document.getElementById("textSearchForm").value;
    let tag = document.getElementById("tagSearchForm").value;
    if (id!==""){
        url = url.concat("id="+id+"&");
    }
    if (author!==""){
        url = url.concat("author="+author+"&");
    }
    if (title!==""){
        url = url.concat("title="+title+"&");
    }
    if (text!==""){
        url = url.concat("description="+text+"&");
    }
    if (tag!==""){
        url = url.concat("tags="+tag);
    } else {
        url = url.slice(0, -1);
    }
    $.ajax({
        url: url,
        type: 'GET',
        success: function() {
            window.location.href= url;
        }
    });
}

function saveItemsOnPage(itemsOnPage) {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    localStorage.setItem("size", itemsOnPage);
    let url = "/management/events?";
    urlSearch.set("size", itemsOnPage);
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}
document.addEventListener("DOMContentLoaded",()=>{
    let dropDownSize = localStorage.getItem("size") || 20;
    $("#dropdown-size").text(dropDownSize);
    changeIcons();
})