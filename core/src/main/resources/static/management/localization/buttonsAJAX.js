function setLanguageEn() {
    let localStorage = window.localStorage;
    localStorage.setItem("language", "en")
    let currentUrl = window.location.href;
    let check = currentUrl.toString();
    if (check.includes("?")){
        let url = "&lang=en";
        $.ajax({
            url: currentUrl + url,
            success: function (res) {
                window.location.href = currentUrl;
            }
        })
    }else {
        let url = "?lang=en";
        $.ajax({
            url: currentUrl + url,
            success: function (res) {
                window.location.href = currentUrl;
            }
        })
    }
}

function setLanguageUa() {
    let localStorage = window.localStorage;
    localStorage.setItem("language", "ua")
    var currentUrl = window.location.href;
    let check = currentUrl.toString();
    if (check.includes("?")){
        let url = "&lang=ua";
        $.ajax({
            url: currentUrl + url,
            success: function (res) {
                window.location.href = currentUrl;
            }
        })
    }else {
        let url = "?lang=ua";
        $.ajax({
            url: currentUrl + url,
            success: function (res) {
                window.location.href = currentUrl;
            }
        })
    }
}