/**
 * Function that transform menu.
 */

function minimize() {
    const menu = document.querySelector(".menuVertical");
    const mainContent = document.querySelector(".main-content");
    const arrow = document.getElementById("maximize");

    menu.classList.toggle("narrow");
    const isNarrow = menu.classList.contains("narrow");

    localStorage.setItem("narrow", isNarrow ? "narrow" : "");
    localStorage.setItem("on", isNarrow ? "on" : "");

    arrow.classList.toggle("on", isNarrow);
    mainContent.style.gridTemplateColumns = `${window.getComputedStyle(menu).width} auto`;
}

/**
 * Script to check and resize menu on minimize button
 * or resize screen.
 */

function resizeMenu() {
    const menu = document.querySelector(".menuVertical");
    const isNarrow = window.innerWidth < 1150;

    if (menu.classList.contains("narrow") !== isNarrow) {
        minimize();
    }
}

// Ініціалізація
document.addEventListener("DOMContentLoaded", () => {
    const menu = document.querySelector(".menuVertical");
    const mainContent = document.querySelector(".main-content");
    const arrow = document.getElementById("maximize");

    if (localStorage.getItem("narrow")) {
        menu.classList.add("narrow");
    }

    if (localStorage.getItem("on")) {
        arrow.classList.add("on");
    }

    mainContent.style.gridTemplateColumns = `${window.getComputedStyle(menu).width} auto`;
});

document.getElementById("maximize").addEventListener("click", minimize);
window.addEventListener("resize", resizeMenu);


/**
 * Script for opening and closing dropdown items.
 */
var acc = document.getElementsByClassName("accordion");

for (var i = 0; i < acc.length; i++) {
    acc[i].addEventListener("click", function () {
        var menu = document.getElementsByClassName("menuVertical")[0];

        var x;
        for (var k = 0; k < acc.length; k++) {
            if (acc[k] !== this) {
                var prev = acc[k].nextElementSibling;
                prev.style.display = "none";
                sessionStorage.removeItem("panel" + k);
            } else {
                x = k;
            }
        }

        var panel = this.nextElementSibling;

        if (menu.classList.contains("narrow")) {
            minimize();
        }
            if (panel.style.display === "block") {
                panel.style.display = "none";
                console.log("will remove storage panel" +x)
                sessionStorage.removeItem("panel" + x);
            } else {
                panel.style.display = "block";
                sessionStorage.setItem("panel" + x, "block");
                console.log("will set storage")
            }
    });
}

var openedPanel = document.getElementsByClassName("panel");
for (var b = 0; b < openedPanel.length; b++) {
    if (sessionStorage.getItem("panel" + b) !== null) {
        openedPanel[b].setAttribute("style", "display: " + sessionStorage.getItem("panel" + b));
    }
}

/**
 * Script for adjusting sidebar height according to height of current page.
 */
window.addEventListener('load', function () {
    var containerH = document.getElementsByClassName("main-content");
    var menuH = document.getElementsByClassName("menuVertical");

    var height = window.getComputedStyle(containerH[0]).height
    var padding = window.getComputedStyle(containerH[0]).paddingTop;
    menuH[0].style.height = parseInt(height, 10) - parseInt(padding, 10) + 'px';
});


/**
 * Script for keeping menu item active
 * when we are transferred by one of links inside this dropdown.
 */

var url = document.URL;
var el = document.getElementById("sidebar");
var links = el.getElementsByTagName("a")

for (var l = 0; l < links.length; l++) {
    if (url.endsWith(links[l].getAttribute("href"))) {
        links[l].classList.toggle("active_li");
    }
}
