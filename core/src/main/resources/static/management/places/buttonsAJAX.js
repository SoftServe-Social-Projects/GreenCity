function clearAllErrorsSpan() {
    $('.errorSpan').text('').hide();
}

function getErrorSpanId(fieldName) {
    if (fieldName.includes('openingHoursList[].')) {
        fieldName = 'openingHoursList';
    }
    return 'errorModalSave' + fieldName;
}

let map;
let markers = [];

function initMap() {
    const mapCenter = {lat: 49.842957, lng: 24.031111};
    map = new google.maps.Map(document.getElementById("map"), {
        zoom: 12,
        center: mapCenter
    });
    // This event listener will call addMarker() when the map is clicked.
    map.addListener("click", (event) => {
        deleteMarkers();
        addMarker(event.latLng);
        document.getElementById('latitude').value = event.latLng.lat();
        document.getElementById('longitude').value = event.latLng.lng();
    });
    // Adds a marker at the center of the map.
    addMarker(mapCenter);
}

$(document).ready(function() {
    $('#locationInputType').change(function() {
        var selectedValue = $(this).val();

        if (selectedValue === 'map') {
            $('#mapSection').show();
            $('#addressSection').hide();
        } else if (selectedValue === 'address') {
            $('#mapSection').hide();
            $('#addressSection').show();
        }
    });
});

// Adds a marker to the map and push to the array.
function addMarker(location) {
    let marker = new google.maps.Marker({
        position: location,
        map: map,
    });
    markers.push(marker);
}

// Deletes all markers in the array by removing references to them.
function deleteMarkers() {
    for (let i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers = [];
}

$(document).ready(function () {

    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    let checkbox = $('table tbody input[type="checkbox"]');
    let actionButtons = $('#btnDelete');

    function toggleActionButtons() {
        let anyChecked = checkbox.filter(':checked').length > 0;
        if (anyChecked) {
            actionButtons.removeClass('disabled').attr('href', '#deleteAllSelectedModal');
        } else {
            actionButtons.addClass('disabled').removeAttr('href');
        }
    }

    $("#selectAll").on('click', function () {
        let isChecked = this.checked;
        checkbox.each(function () {
            if (!this.disabled) {
                this.checked = isChecked;
            }
        });
        toggleActionButtons();
    });

    checkbox.on('click', function () {
        if (!this.checked) {
            $("#selectAll").prop("checked", false);
        }
        toggleActionButtons();
    });

    // Add place button (popup)
    $('#addPlaceModalBtn').on('click', function (event) {
        clearAllErrorsSpan();
        clearEditModal();
        $('#submitAddBtn').val("Add");
        $('.modal-title').text("Add Place");
    });

    $('#addDiscount').on('click', addDiscountValue);
    $(document).on('click', '.remove', function () {
        $(this).closest('.discount').remove();
    });

    $(document).on('click', '.add-break', function (event) {
        event.preventDefault();
        $(this).hide();
        $(this).closest('.break-time').find('.break-hours').show();
    });
    $(document).on('click', '.removeBreak', function (event) {
        event.preventDefault();
        $(this).closest('.break-time').find('.add-break').show();
        $(this).closest('.break-hours').find('input').val('');
        $(this).closest('.break-time').find('.break-hours').hide();
    });

    function addDiscountValue(event) {
        event.preventDefault();
        let specificationOption = '';
        for (let i = 0; i < discountSpecifications.length; i++) {
            specificationOption += '<option value="' + discountSpecifications[i] + '">' + discountSpecifications[i] + '</option>';
        }
        let discountValueInput = "<input name='discountValue' class='form-control' type='number'>";
        let removeButton = "<button class='btn btn-warning remove'>remove</button>"
        let discDiv = "<div class='discount form-inline'>" +
            "<select class='form-control'>" + specificationOption + "</select>" +
            discountValueInput +
            removeButton + "</div>"
        $('#discounts').append(discDiv);
    }

// Submit button in addPlaceModal
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();

        let isValid = validateForm();
        if (!isValid) {
            return;
        }

        let formData = new FormData(document.getElementById('addPlaceForm'));
        let type = $('#id').val() ? 'PUT' : 'POST';

        if (type === 'POST') {
            handlePostRequest(formData);
        } else {
            handlePutRequest(formData);
        }
    });

    function handlePostRequest(formData) {
        const place = {
            "placeName": formData.get('name'),
            "locationName": formData.get('address'),
            "status": formData.get('status'),
            "categoryName": formData.get('category'),
            "discountValues": getDiscountValues(),
            "openingHoursList": getOpeningHours()
        };

        submitPostFormData('/management/places', 'POST', formData, place);
    }

    function handlePutRequest(formData) {
        const place = {
            id: formData.get('id'),
            name: formData.get('name'),
            location: {
                address: formData.get('address'),
                lat: formData.get('lat'),
                lng: formData.get('lng'),
                addressUa: formData.get('addressUa'),
            },
            category: {
                name: formData.get('category'),
            },
            discountValues: getDiscountValues(),
            openingHoursList: getOpeningHours(),
        };

        submitPutFormData('/management/places', 'PUT', formData, place);
    }

    function submitPostFormData(url, method, formData, place) {
        formData.append('addPlaceDto', JSON.stringify(place));
        const file = document.getElementById("creationFile").files[0];
        if (file) {
            formData.append("images", file);
        }

        sendAjaxRequest(url, method, formData);
    }

    function submitPutFormData(url, method, formData, place) {
        formData.append('placeUpdateDto', JSON.stringify(place));
        const fileInput = document.getElementById('creationFile');

        Array.from(fileInput?.files || []).forEach((file) => {
            formData.append('images', file);
        });

        sendAjaxRequest(url, method, formData);
    }

    function sendAjaxRequest(url, method, formData) {
        $.ajax({
            url: url,
            type: method,
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById(getErrorSpanId(el.fieldName))).text(el.fieldError).show();
                    });
                } else {
                    location.reload();
                }
            },
            error: function (xhr, status, error) {
                console.error('XHR Status: ' + xhr.status);
                console.error('Error: ' + error);
                console.error('Response Text: ' + xhr.responseText);

                let errorMessage = `Error status: ${xhr.status} - ${error}`;
                if (xhr.responseText) {
                    try {
                        const response = JSON.parse(xhr.responseText);
                        errorMessage += `\nMessage: ${response.message || 'Unknown error'}`;
                    } catch (e) {
                        errorMessage += `\nMessage: ${xhr.responseText}`;
                    }
                }

                alert(errorMessage);
            }
        });
    }

    function validateForm() {
        let isValid = true;

        const requiredFields = [
            { id: 'address', errorId: 'errorModalSavelocation', message: messages["greenCity.places.page.add.address"] },
            { id: 'placeName', errorId: 'errorModalSavename', message: messages["greenCity.places.page.add.place.name"] },
            { id: 'category', errorId: 'errorModalSavecategory', message: messages["greenCity.places.page.add.category"] },
        ];

        requiredFields.forEach(field => {
            let value = $(`#${field.id}`).val();
            if (!value) {
                $(`#${field.errorId}`).text(field.message).show();
                isValid = false;
            }
        });


        let openingHoursChecked = false;
        $('input[name="day"]:checked').each(function () {
            let row = $(this).closest('.form-row');
            let openTime = row.find('input[name="openTime"]').val();
            let closeTime = row.find('input[name="closeTime"]').val();

            if (!openTime || !closeTime) {
                $('#errorModalSaveopeningHoursList').text(messages["greenCity.places.page.add.working.hovers"]).show();
                isValid = false;
            } else {
                let openTimeMinutes = timeToMinutes(openTime);
                let closeTimeMinutes = timeToMinutes(closeTime);

                if (closeTimeMinutes - openTimeMinutes < 30) {
                    $('#errorModalSaveopeningHoursList').text(messages["greenCity.places.page.hovers.is.incorrect"]).show();
                    isValid = false;
                }
            }
            openingHoursChecked = true;
        });

        if (!openingHoursChecked) {
            $('#errorModalSaveopeningHoursList').text(messages["greenCity.places.page.add.day.hovers"]).show();
            isValid = false;
        }

        return isValid;
    }

    function timeToMinutes(time) {
        let [hours, minutes] = time.split(':').map(Number);
        return hours * 60 + minutes;
    }

    function getDiscountValues() {
        let discounts = [];
        $('#discounts').find('.discount').each(function () {
            let specification = {
                name: $(this).find(':selected').text()
            };
            let discount = {};
            discount.specification = specification;
            discount.value = $(this).find('input[name="discountValue"]').val();
            discounts.push(discount);
        });
        if (!discounts.length) {
            discounts = null;
        }
        return discounts;
    }

    function getOpeningHours() {
        let openingHours = [];
        $("input:checkbox[name=day]:checked").each(function () {
            let openHour = {};
            openHour.weekDay = $(this).next().find('span').text();
            openHour.openTime = $(this).closest('div.form-row').find('input[name=openTime]').val();
            openHour.closeTime = $(this).closest('div.form-row').find('input[name=closeTime]').val();
            let breakTime = {};
            breakTime.startTime = $(this).closest('div.form-row').find('input[name=startTime]').val();
            breakTime.endTime = $(this).closest('div.form-row').find('input[name=endTime]').val();
            if (breakTime.startTime === "" || breakTime.endTime === "") {
                breakTime = null;
            }
            openHour.breakTime = breakTime;
            openingHours.push(openHour);
        });
        if (!openingHours.length) {
            openingHours = null;
        }
        return openingHours;
    }

    $('td .delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deletePlaceModal').modal();
        let href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });

    //delete в deletePlaceModal
    $('#deleteOneSubmit').on('click', function (event) {
        event.preventDefault();
        let href = $(this).attr('href');
        $.ajax({
            url: href,
            type: 'delete',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            },
        });
    });
    //delete в deleteAllSelectedModal
    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        let payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        })
        let href = '/management/places/deleteAll';
        $.ajax({
            url: href,
            type: 'delete',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                $('#deleteAllSelectedModal').modal('toggle');
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    });

    function clearEditModal() {
        $('input[name=day]').prop('checked', false);
        $('#addPlaceModal').find('input').not('input[name=status]').not('#submitAddBtn').val('');
        $('#empty-category').prop("selected", true);
        deleteMarkers();
        $('.discount').remove();
    }

    // Button edit
    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $('#submitAddBtn').val("Edit");
        $('.modal-title').text("Edit Place");
        clearEditModal();
        $('#addPlaceModal').modal();
        let href = $(this).attr('href');
        $.get(href, function (place) {
            $('#id').val(place.id)
            $('#placeName').val(place.name);
            $('#lng').val(place.location.lng);
            $('#lat').val(place.location.lat);
            $('#addressUa').val(place.location.addressUa);
            $('#address').val(place.location.address);
            addMarker(location);
            place.openingHoursList.forEach(function (day) {
                let dayElement = $(`#${day.weekDay}`);
                dayElement.prop('checked', true);
                dayElement.closest('div.form-row').find('input[name=openTime]').val(day.openTime);
                dayElement.closest('div.form-row').find('input[name=closeTime]').val(day.closeTime);
                if (day.breakTime !== null) {
                    dayElement.closest('div.form-row').find('.add-break').hide();
                    dayElement.closest('div.form-row').find('.break-hours').show();
                    dayElement.closest('div.form-row').find('input[name=startTime]').val(day.breakTime.startTime);
                    dayElement.closest('div.form-row').find('input[name=endTime]').val(day.breakTime.endTime);
                }
            });
            $('#category').val(place.category.name);
            place.discountValues.forEach(value => {
                addDiscountValueForUpdate(value);
            });
        });
    });

    function addDiscountValueForUpdate(discount) {
        let specificationOption = '';
        for (let i = 0; i < discountSpecifications.length; i++) {
            if (discountSpecifications[i] === discount.specification.name) {
                specificationOption += `<option value=${discountSpecifications[i]} selected="true">${discountSpecifications[i]}</option>`
            } else {
                specificationOption += `<option value=${discountSpecifications[i]}>${discountSpecifications[i]}</option>`
            }
        }
        let discValue = discount.value;
        let discountValueInput = '<input name="discountValue" class="form-control" type="number" value="' + discValue + '"/>';

        let removeButton = "<button class='btn btn-warning remove'>remove</button>"
        let discDiv = "<div class='discount form-inline'>" +
            "<select class='form-control'>" + specificationOption + "</select>" +
            discountValueInput +
            removeButton + "</div>"
        $('#discounts').append(discDiv);
    }
});

$(document).ready(function () {
    $('.filter-container').hide();
    setPageSizeFromLocalStorage();
    $(document).on('click', function (e) {
        if (!$(e.target).closest('th').length) {
            $('.filter-container').hide();
            $('.eFilterBtn').removeClass('active');
        }
    });

    $('.eFilterBtn').on('click', function () {
        const $filterContainer = $(this).closest('th').find('.filter-container');
        $('.eFilterBtn').not(this).removeClass('active');
        $('.filter-container').not($filterContainer).hide();
        $(this).toggleClass('active');
        $filterContainer.toggle();
    });
});

function getUrlSearchParams() {
    const allParam = window.location.search;
    const urlSearch = new URLSearchParams(allParam);

    const params = {
        id: urlSearch.get("id"),
        status: urlSearch.get("status"),
        name: urlSearch.get("name"),
        author: urlSearch.get("author"),
        address: urlSearch.get("address"),
    };

    if (params.page !== null) {
        params.page = "0";
    }

    const newUrlSearch = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
        if (value !== null) {
            newUrlSearch.set(key, value);
        }
    });
    if (urlSearch.has("sort")) {
        const sortParams = urlSearch.getAll("sort").filter(Boolean);
        sortParams.forEach(param => newUrlSearch.append("sort", param));
    }

    return newUrlSearch;
}

function searchByNameField(searchValue, fieldName, searchValue2 = null, fieldName2 = null) {
    let urlSearch = getUrlSearchParams();
    if (searchValue !== null && searchValue !== "") {
        urlSearch.set(fieldName, searchValue);
    }
    if (searchValue2 !== null && searchValue2 !== "") {
        urlSearch.set(fieldName2, searchValue2);
    }

    let url = "/management/places?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function removeFilter(filterName) {
    let urlSearch = getUrlSearchParams();
    console.log(urlSearch.toString())
    urlSearch.delete(filterName);
    console.log(urlSearch.toString())
    let newUrl = "/management/places?" + urlSearch.toString();

    if (!urlSearch.toString()) {
        newUrl = "/management/places";
    }
    window.location.href = newUrl;
}


function orderByNameField(sortOrder, fieldName) {
    const urlSearch = getUrlSearchParams();
    urlSearch.delete("sort");
    urlSearch.append("sort", `${fieldName},${sortOrder}`);
    const url = "/management/places?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function () {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function saveItemsOnPage(itemsOnPage) {
    var allParam = window.location.search;
    localStorage.setItem('pageSize', itemsOnPage);
    document.getElementById('currentPageSize').innerText = itemsOnPage;
    console.log(itemsOnPage)
    var urlSearch = new URLSearchParams(allParam);
    localStorage.setItem("size", itemsOnPage);
    let url = "/management/places?";
    urlSearch.set("size", itemsOnPage);
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function setPageSizeFromLocalStorage() {
    let pageSize = localStorage.getItem('pageSize') || 20;
    document.getElementById('currentPageSize').innerText = pageSize;
}

function searchByQuery(query) {
    if (query === null || query === '') {
        removeFilter('query');
    }
    let urlSearch = getUrlSearchParams();
    urlSearch.set("query", query);

    let url = "/management/places?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function getSelectedStatus() {
    const radios = document.getElementsByName('status');
    for (const radio of radios) {
        if (radio.checked) {
            return radio.value;
        }
    }
    return '';
}

