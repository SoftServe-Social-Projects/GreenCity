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

// Adds a marker to the map and push to the array.
function addMarker(location) {
    const marker = new google.maps.Marker({
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
    });

    $('#addDiscount').on('click', addDiscountValue);
    $(document).on('click', '.remove', function () {
        $(this).closest('.discount').remove();
    });

    $(document).on('click', '.addBreakTime', addBreakTime);
    $(document).on('click', '.removeBreak', function (event) {
        event.preventDefault();
        $(this).closest('.breaks').find('.addBreakTime').show();
        $(this).closest('.break').remove();
    });

    function addBreakTime(event) {
        $(this).hide();
        event.preventDefault();
        let startTimeInput = "<input name='startTime'  class='form-control-sm' type='time'>";
        let endTimeInput = "<input name='endtTime' class='form-control-sm' type='time'>";
        let removeButton = '<i class="material-icons removeBreak" data-toggle="tooltip"  title="Delete break hours">&#xE872;</i>'
        let startDiv = "<div class='text-right'>" + "<label>Break(Start)</label>" +
            startTimeInput + " </div>"
        let endDiv = "<div class='text-right'>" + "<label>Break(End)</label>" +
            endTimeInput + "</div>"
        let breakDiv = "<div class='break form-inline'>" + startDiv + endDiv + removeButton
        $(this).closest('.breaks').append(breakDiv);
    }

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
        let formData = $('#addPlaceForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        let newPlace = {
            "name": formData.name,
            "location":
                {
                    "address": formData.address,
                    "lat": formData.latitude,
                    "lng": formData.longitude
                },
            "status": formData.status,
            "category": formData.category,
            "photo": formData.photo
        }
        newPlace.discountValues = getDiscountValues();
        newPlace.openingHoursList = getOpeningHours();

        // Ajax request
        $.ajax({
            url: '/management/places/',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById(getErrorSpanId(el.fieldName))).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(newPlace)
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

    $('td .delete.eDelBtn').on('click',function(event){
        event.preventDefault();
        $('#deletePlaceModal').modal();
        let href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href',href);
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
});