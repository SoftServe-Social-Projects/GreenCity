
function clearAllErrorsSpan() {
    $('.errorSpan').text('');
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

    // Select/Deselect checkboxes
    let checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").on('click', function () {
        if (this.checked) {
            checkbox.each(function () {
                if (!this.disabled) {
                    this.checked = true;
                }
            });
        } else {
            checkbox.each(function () {
                this.checked = false;
            });
        }
    });
    checkbox.on('click', function () {
        if (!this.checked) {
            $("#selectAll").prop("checked", false);
        }
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

        let formData = new FormData(document.getElementById('addPlaceForm'));

        let place;
        let type = $('#id').val() ? 'PUT' : 'POST';

        if (type === 'POST') {
            place = {
                "placeName": formData.get('name'),
                "locationName": formData.get('address'),
                "status": formData.get('status'),
                "categoryName": formData.get('category')
            };
        } else {
            place = {
                "id": formData.get('id'),
                "placeName": formData.get('name'),
                "locationName": formData.get('address'),
                "status": formData.get('status'),
                "categoryName": formData.get('category')
            };
        }

        place.discountValues = getDiscountValues();
        place.openingHoursList = getOpeningHours();

        formData.append('addPlaceDto', JSON.stringify(place));
        var file = document.getElementById("creationFile").files[0];
        console.log(file);
        formData.append("images", file);
        $.ajax({
            url: '/management/places',
            type: type,
            data: formData,
            processData: false,
            contentType: false,
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById(getErrorSpanId(el.fieldName))).text(el.fieldError);
                    });
                } else {
                    location.reload();
                }
            },
            error: function (xhr, status, error) {
                console.error(error);
                alert('Error');
            }
        });
    });

    function getErrorSpanId(fieldName) {
        if (fieldName.includes('openingHoursList[].')) {
            fieldName = 'openingHoursList';
        }
        return 'errorModalSave' + fieldName;
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
            $('#address').val(place.location.address);
            $('input[name=latitude]').val(place.location.lat);
            $('input[name=longitude]').val(place.location.lng);
            deleteMarkers();
            let location = {
                lat: place.location.lat,
                lng: place.location.lng
            };
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
    const sortParamKey = 'sortParam';
    const sortDirectionKey = 'sortDirection';

    function switchSort(sort) {
        return sort === "asc" ? "desc" : "asc";
    }

    function getSortDirection() {
        const savedSortDirection = localStorage.getItem(sortDirectionKey);
        return savedSortDirection ? savedSortDirection : 'asc';
    }

    function initTableSorting() {
        let isSorting = false;

        const savedSortParam = localStorage.getItem(sortParamKey);
        const savedSortDirection = getSortDirection();

        if (savedSortParam) {
            $(`[data-sort-param="${savedSortParam}"]`).addClass(savedSortDirection);
        }
        $('.table-container').on('click', '.sort-icon', function () {
            if (isSorting) return;

            let $icon = $(this);
            let sortParam = $icon.data('field');
            let currentSortDirection = getSortDirection();
            let newSortDirection = switchSort(currentSortDirection);

            localStorage.setItem(sortParamKey, sortParam);
            localStorage.setItem(sortDirectionKey, newSortDirection);

            $('.table-filter-icon').removeClass('asc desc');
            $icon.addClass(newSortDirection);

            isSorting = true;
            $.ajax({
                url: '/management/places',
                type: 'GET',
                data: {
                    sort: sortParam + ',' + newSortDirection
                },
                success: function (data) {
                    $('#placeTable').html($(data).find('#placeTable').html());
                },
                error: function (xhr, status, error) {
                    console.error("AJAX Error:", error);
                },
                complete: function () {
                    isSorting = false;
                }
            });
        });
    }

    initTableSorting();
});
