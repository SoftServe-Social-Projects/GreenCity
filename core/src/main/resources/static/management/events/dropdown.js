"use strict";

function toggleDropdown(headerElement) {
    const dropdownContent = headerElement.nextElementSibling;
    const arrow = headerElement.querySelector(".dropdown__arrow");
    dropdownContent.style.display = dropdownContent.style.display === "none" ? "block" : "none";
    arrow.textContent = dropdownContent.style.display === "block" ? "▲" : "▼";
}
