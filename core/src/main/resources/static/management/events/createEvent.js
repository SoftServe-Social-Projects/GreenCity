const quill = initializeQuilEditor();
const durationSelect = document.getElementById('duration');
const daysInfoDiv = document.getElementById('daysInfo');
const form = document.getElementById('addEventsForm');
const imageUploader = initializeImageUploader();

quill.on('text-change', function () {
    updateHiddenInput(quill);
    validateQuillContent(quill);
});

generateDayInfoElements(daysInfoDiv, durationSelect.value);
durationSelect.addEventListener('change', function () {
    generateDayInfoElements(daysInfoDiv, durationSelect.value);
});

document.addEventListener('DOMContentLoaded', function () {
    const confirmationModal = new ConfirmationModal('confirmationModal', 'confirmationMessage', 'confirmModalButton', "cancelModalButton");

    imageUploader.loadProposedImages([
        '/img/events/default-image.png',
        '/img/events/illustration-earth.png',
        '/img/events/illustration-money.png',
        '/img/events/illustration-people.png',
        '/img/events/illustration-recycle.png',
        '/img/events/illustration-store.png',
    ]);

    [document.getElementById('cancelButton'), document.getElementById('goBackButton')].forEach(el => el?.addEventListener('click', () => {
            confirmationModal.show(
                'Are you sure you want to cancel? All unsaved data will be lost.',
                () => {
                    form.reset();
                    window.location.href = backendAddress + "/management/events";
                },
                () => {
                }
            );
        })
    );

    document.getElementById("previewBtn")?.addEventListener('click', () => {
        confirmationModal.show(
            'Are you sure you want to open preview?',
            () => {
                if (formValidation()) {
                    const eventModal = new bootstrap.Modal(document.getElementById("eventModalPreview"));

                    generateEventPreview(getEventPreviewData());
                    eventModal.show()
                }
            },
            () => {

            }
        );
    })

    document.getElementById('submitBtn')?.addEventListener('click', () => {
        confirmationModal.show(
            'Are you sure you want to add this event?',
            () => {
                if (formValidation()) {
                    submitEventForm();
                }
            },
            () => {

            }
        );
    });

    document?.getElementById('updateBtn').addEventListener('click', () => {
        confirmationModal.show(
            'Are you sure you want to update this event?',
            () => {
                if (formValidation()) {
                    submitUpdateEventForm();
                }
            },
            () => {

            }
        );
    });

});

function initializeQuilEditor() {
    return new Quill('#editor-container', {
        theme: 'snow',
        modules: {
            toolbar: [
                [{'font': []}],
                [{'size': []}],
                ['bold', 'italic', 'underline'],
                [{'align': []}],
                [{'color': []}, {'background': []}],
            ]
        }
    });
}

function initializeImageUploader() {
    return new ImageUploader({
        fileInputSelector: '#imageUpload',
        previewContainerSelector: '#previewContainer',
        proposedImagesContainerSelector: '#proposedImages',
        errorMessageSelector: '#uploadImagesErrorMessage',
        maxFiles: 5
    });
}

function updateHiddenInput(quill) {
    const editorContent = quill.root.innerHTML;
    document.getElementById('editor-content').value = editorContent;
}


function validateDayInput() {
    const dateValue = this.value;
    const dateSplit = dateValue.split('-');

    const dateYear = dateSplit[0];
    const dateMonth = dateSplit[1];
    const dateDay = dateSplit[2];

    if (dateYear.length > 4) {
        const newDate = dateYear.substring(0, 4) + '-' + dateMonth + '-' + dateDay;
        this.value = newDate;
    }
}

function generateDayInfoElements(container, duration = '1') {
    container.innerHTML = '';
    const durationValue = parseInt(duration);
    for (let i = 0; i < durationValue; i++) {
        const dayInfoDiv = createDayInfoElement(i);
        dayInfoDiv.setAttribute('data-day-index', i);
        container.appendChild(dayInfoDiv);
        handleCheckboxChange(i);
        const date = document.getElementById(`date-${i}`)
        date.addEventListener('keyup', validateDayInput);
    }
    for (let i = 0; i < durationValue; i++) {
        initMap(i);
    }
}

function createDayInfoElement(i) {
    const dayInfoDiv = document.createElement('div');
    dayInfoDiv.classList.add('dayInfo');
    dayInfoDiv.innerHTML = `
        <h3 class="display-5">Day ${i + 1}</h3>
        <div class="form-group">
            <label for="date-${i}">Date:</label>
            <input type="date" id="date-${i}" class="form-control" required />
            <p id="date-error-${i}" class="form-error" style="display: none;"></p>
        </div>
        <!-- All Day Checkbox -->
        <div class="form-group">
            <input class="form-check-input" type="checkbox" id="allDay-${i}" />
            <label class="form-check-label" for="allDay-${i}">All Day</label>
        </div>
        <!-- Start Time -->
        <div class="form-group">
            <label for="startTime-${i}">Start Time:</label>
            <input type="time" id="startTime-${i}" class="form-control" required />
            <p id="start-time-error-${i}" class="form-error" style="display: none;">Start Time is required</p>
        </div>

        <!-- End Time -->
        <div class="form-group">
            <label for="finishTime-${i}">Finish Time:</label>
            <input type="time" id="finishTime-${i}" class="form-control" required />
            <p id="finish-time-error-${i}" class="form-error" style="display: none;">Finish Time is required</p>
        </div>
        <!-- Place -->
        <div class="form-group">
            <input class="form-check-input" type="checkbox" id="toggleLocation-${i}" />
            <label class="form-check-label" for="toggleLocation-${i}">Place</label>
            <div id="location-fields-${i}" style="display: none;">
                <label for="autocomplete-${i}">Event Location:</label>
                <input id="autocomplete-${i}" style="margin-bottom:30px" class="form-control" placeholder="Enter your address" type="text" />
                <input type="hidden" id="latitude-${i}"  name="addEventDtoRequest.datesLocations[${i}].coordinates.latitude" />
                <input type="hidden" id="longitude-${i}" name="addEventDtoRequest.datesLocations[${i}].coordinates.longitude" />
                <p class="form-error" id="location-error-${i}"></p>
                <div id="map-${i}" style="height: 400px;"></div>
            </div>
        </div>
        <!-- Link -->
        <div class="form-group">
            <input class="form-check-input" type="checkbox" id="toggleOnlineLink-${i}" />
            <label class="form-check-label" for="toggleOnlineLink-${i}">Online Link</label>
            <div id="online-link-fields-${i}" style="display: none;">
                <label for="onlineLink-${i}">Online:</label>
                <input type="url" id="onlineLink-${i}" class="form-control" name="addEventDtoRequest.datesLocations[${i}].onlineLink" placeholder="Enter online link (if any)" />
                <p class="form-error" id="onlineLink-${i}-error"></p>
            </div>
            <p class="form-error" id="onlineLink-location-${i}-error"></p>
        </div>
            `;

    return dayInfoDiv;
}

function handleCheckboxChange(i) {
    const toggleLocationCheckbox = document.getElementById(`toggleLocation-${i}`);
    const locationFields = document.getElementById(`location-fields-${i}`);
    const toggleOnlineLinkCheckbox = document.getElementById(`toggleOnlineLink-${i}`);
    const onlineLinkFields = document.getElementById(`online-link-fields-${i}`);
    const allDayCheckBox = document.getElementById(`allDay-${i}`);

    locationFields.style.display = toggleLocationCheckbox.checked ? 'block' : 'none';
    onlineLinkFields.style.display = toggleOnlineLinkCheckbox.checked ? 'block' : 'none';

    toggleLocationCheckbox.addEventListener('change', function () {
        locationFields.style.display = toggleLocationCheckbox.checked ? 'block' : 'none';
    });

    toggleOnlineLinkCheckbox.addEventListener('change', function () {
        onlineLinkFields.style.display = toggleOnlineLinkCheckbox.checked ? 'block' : 'none';
    });

    allDayCheckBox.addEventListener('change', function () {
        handleAllDayCheckbox(i);
    });

}

function handleAllDayCheckbox(i) {
    const allDayCheckbox = document.getElementById(`allDay-${i}`);
    const startTimeField = document.getElementById(`startTime-${i}`);
    const finishTimeField = document.getElementById(`finishTime-${i}`);

    if (allDayCheckbox.checked) {
        startTimeField.value = '00:00';
        finishTimeField.value = '23:59';
        startTimeField.disabled = true;
        finishTimeField.disabled = true;
    } else {
        startTimeField.disabled = false;
        finishTimeField.disabled = false;
    }
}

function initMap(i) {
    const ukraine = {lat: 48.3794, lng: 31.1656};
    const mapElement = document.getElementById(`map-${i}`)
    const map = new google.maps.Map(mapElement, {
        center: ukraine,
        zoom: 6,
    });
    mapElement.googleMap = map;
    const input = document.getElementById(`autocomplete-${i}`);
    const latitudeInput = document.getElementById(`latitude-${i}`);
    const longitudeInput = document.getElementById(`longitude-${i}`);

    const autocomplete = new google.maps.places.Autocomplete(input, {
        types: ['geocode'],
        componentRestrictions: {country: 'ua'},
        fields: ['place_id', 'geometry', 'formatted_address']
    });

    const marker = new google.maps.Marker({
        map: map,
        draggable: true
    });
    marker.setMap(map)
    mapElement.marker = marker;
    const geocoder = new google.maps.Geocoder();

    autocomplete.addListener('place_changed', function () {
        const place = autocomplete.getPlace();

        if (!place.geometry) {
            console.log("Autocomplete's returned place contains no geometry");
            return;
        }

        map.setCenter(place.geometry.location);
        map.setZoom(12);

        marker.setPosition(place.geometry.location);
        marker.setVisible(true);

        input.value = place.formatted_address;

        latitudeInput.value = place.geometry.location.lat();
        longitudeInput.value = place.geometry.location.lng();
    });

    map.addListener('click', function (event) {
        const latLng = event.latLng;

        geocoder.geocode({location: latLng}, function (results, status) {
            if (status === 'OK' && results[0]) {
                const place = results[0];
                const address = place.formatted_address;

                input.value = address;

                marker.setPosition(latLng);
                marker.setVisible(true);

                latitudeInput.value = latLng.lat();
                longitudeInput.value = latLng.lng();
            } else {
                console.log('Geocoder failed: ' + status);
            }
        });
    });

    marker.addListener('dragend', function (event) {
        const latLng = event.latLng;

        geocoder.geocode({location: latLng}, function (results, status) {
            if (status === 'OK' && results[0]) {
                const place = results[0];
                input.value = place.formatted_address;

                latitudeInput.value = latLng.lat();
                longitudeInput.value = latLng.lng();
            } else {
                console.log('Geocoder failed: ' + status);
            }
        });
    });
}

function submitEventForm() {
    const formData = createFormData();

    fetch(`${backendAddress}/management/events`, {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok' + response.status);
            }
            window.location.href = backendAddress + "/management/events";
        })
        .catch(error => console.error('Error:', error));

}

function createFormData() {
    const title = document.getElementById("title").value;
    const description = quill.root.innerHTML;
    const tags = Array.from(document.getElementById("initiatives").querySelectorAll('.form-check-input:checked')).map(tag => tag.value);
    const isOpen = Boolean(document.getElementById('eventType').value);

    const addEventDtoRequest = {
        title: title,
        description: description,
        tags: tags,
        open: isOpen,
        datesLocations: []
    };
    const daysInfoDiv = document.getElementById('daysInfo');
    const dayInfos = daysInfoDiv.querySelectorAll('.dayInfo');

    dayInfos.forEach((dayInfo, index) => {
        const date = dayInfo.querySelector(`#date-${index}`).value;
        const startTime = dayInfo.querySelector(`#startTime-${index}`).value;
        const finishTime = dayInfo.querySelector(`#finishTime-${index}`).value;

        const locationCheckbox = dayInfo.querySelector(`#toggleLocation-${index}`);
        const onlineLinkCheckbox = dayInfo.querySelector(`#toggleOnlineLink-${index}`);

        const latitude = locationCheckbox.checked ? dayInfo.querySelector(`#latitude-${index}`).value : null;
        const longitude = locationCheckbox.checked ? dayInfo.querySelector(`#longitude-${index}`).value : null;
        const onlineLink = onlineLinkCheckbox.checked ? dayInfo.querySelector(`#onlineLink-${index}`).value : null;

        const startDateTime = combineDateTime(date, startTime);
        const finishDateTime = combineDateTime(date, finishTime);
        const locationData = (latitude && longitude) ? {
            coordinates: {
                latitude: parseFloat(latitude) || null,
                longitude: parseFloat(longitude) || null,
            }
        } : {};

        const onlineLinkData = onlineLink ? {
            onlineLink: onlineLink
        } : {};

        addEventDtoRequest.datesLocations.push({
            startDate: startDateTime,
            finishDate: finishDateTime,
            ...locationData,
            ...onlineLinkData
        });
    });

    const formData = new FormData();
    formData.append('addEventDtoRequest', new Blob([JSON.stringify(addEventDtoRequest)], {type: 'application/json'}));

    const images = imageUploader.currentFiles;

    for (let i = 0; i < images.length; i++) {
        formData.append('images', images[i]);
    }
    return formData;
}

function combineDateTime(date, time) {
    return date + 'T' + time + ':00Z';
}

function formValidation() {
    let isValid = true;
    const dayInfos = document.querySelectorAll(".dayInfo");
    isValid &= validateTitle();
    isValid &= validateQuillContent(quill);
    isValid &= validateImages();
    isValid &= validateInitiativeType();
    isValid &= validateEventType();
    isValid &= validateInvitees();
    isValid &= validateAllDays(dayInfos.length);
    return isValid
}

function validateTitle() {
    const title = document.getElementById('title').value;
    const titleErrorElement = document.getElementById('title-error');
    if (title.trim() === '') {
        displayError(titleErrorElement, 'Please insert up to 170 symbols');
        return false;
    } else if (title.length > 170) {
        displayError(titleErrorElement, 'Please fill in 170 symbols maximum');
        return false;
    } else {
        hideError(titleErrorElement);
        return true;
    }
}

function validateDate(i) {
    const dateField = document.getElementById(`date-${i}`);
    const dateError = document.getElementById(`date-error-${i}`);
    let isValid = true;

    hideError(dateError);

    if (!dateField.value) {
        displayError(dateError, "Date is required");
        isValid = false;
    } else {
        const dateValue = new Date(dateField.value);
        const today = new Date().setHours(0, 0, 0, 0);

        if (isNaN(dateValue.getTime())) {
            displayError(dateError, "Invalid date format");
            isValid = false;
        } else if (dateValue < today) {
            displayError(dateError, "Date can't be earlier than today");
            isValid = false;
        }
    }

    return isValid;
}

function validateTime(i) {
    const startTimeField = document.getElementById(`startTime-${i}`);
    const finishTimeField = document.getElementById(`finishTime-${i}`);
    const startTimeError = document.getElementById(`start-time-error-${i}`);
    const finishTimeError = document.getElementById(`finish-time-error-${i}`);
    let isValid = true;

    hideError(startTimeError);
    hideError(finishTimeError);

    if (startTimeField.value || finishTimeField.value) {
        if (!startTimeField.value.match(/^([01]\d|2[0-3]):([0-5]\d)$/)) {
            displayError(startTimeError, "Invalid start time format");
            isValid = false;
        }
        if (!finishTimeField.value.match(/^([01]\d|2[0-3]):([0-5]\d)$/)) {
            displayError(finishTimeError, "Invalid finish time format");
            isValid = false;
        }

        if (startTimeField.value && finishTimeField.value) {
            if (startTimeField.value > finishTimeField.value) {
                displayError(startTimeError, "Start time must be earlier than finish time");
                displayError(finishTimeError, "Finish time must be later than start time");
                isValid = false;
            }
        }
    }

    return isValid;
}

function validateDateConsistency(totalDays) {
    let isValid = true;

    for (let i = 1; i < totalDays; i++) {
        const prevDateElement = document.getElementById(`date-${i - 1}`);
        const currentDateElement = document.getElementById(`date-${i}`);

        if (prevDateElement && currentDateElement) {
            const prevDate = prevDateElement.value;
            const currentDate = currentDateElement.value;

            if (prevDate && currentDate) {
                const prevDateTime = new Date(prevDate);
                const currentDateTime = new Date(currentDate);

                if (currentDateTime < prevDateTime) {
                    displayError(document.getElementById(`date-error-${i}`), "Date must be later than the previous day's date");
                    isValid = false;
                } else {
                    hideError(document.getElementById(`date-error-${i}`));
                }
            }
        }
    }

    return isValid;
}

function validateAllDays(totalDays) {
    let isValid = true;
    for (let i = 0; i < totalDays; i++) {
        let onlineLinkPlaceCheckBoxError = document.getElementById(`onlineLink-location-${i}-error`)
        if (!isOnlineLinkSelected(i) && !isPlaceSelected(i)) {
            displayError(onlineLinkPlaceCheckBoxError, "Please select online link or/and location")
            isValid = false;
        } else {
            hideError(onlineLinkPlaceCheckBoxError)
        }
        isValid &= validateLink(i)
        isValid &= validateCoordinates(i)
        isValid &= validateDate(i);
        isValid &= validateTime(i)
    }
    isValid &= validateDateConsistency(totalDays);
    return isValid;
}


function isPlaceSelected(i) {
    return document.getElementById(`toggleLocation-${i}`).checked;
}

function isOnlineLinkSelected(i) {
    return document.getElementById(`toggleOnlineLink-${i}`).checked;
}

function validateQuillContent(quill) {
    const minLength = 20;
    const maxLength = 63206;
    const quillLength = quill.getText();
    const errorElement = document.getElementById('quill-error');
    if (quillLength.trim().length < minLength) {
        displayError(errorElement, 'Content must be at least ' + minLength + ' characters long.');
        return false;
    } else if (quillLength > maxLength) {
        displayError(errorElement, 'Content must be less than ' + maxLength + ' characters long.');
        return false
    } else {
        hideError(errorElement);
        return true
    }
}

function validateImages() {
    const imagesErrorElement = document.getElementById("images-error");
    if (imageUploader.currentFiles.length === 0) {
        displayError(imagesErrorElement, "Please add at least one image")
        return false;
    } else {
        hideError(imagesErrorElement);
        return true
    }
}

function validateInitiativeType() {
    const checkboxes = document.querySelectorAll('input[name="tags"]:checked');
    const initiativeErrorElement = document.getElementById('initiative-error');
    if (checkboxes.length === 0) {
        displayError(initiativeErrorElement, 'Please select at least one Initiative type');
        return false;
    } else {
        hideError(initiativeErrorElement);
        return true;
    }
}

function validateEventType() {
    const eventType = document.getElementById('eventType').value;
    const eventTypeErrorElement = document.getElementById('eventType-error');
    if (eventType === '') {
        displayError(eventTypeErrorElement, 'Please select Event Type');
        return false;
    } else {
        hideError(eventTypeErrorElement);
        return true;
    }
}

function validateInvitees() {
    const invitees = document.getElementById('invitees').value;
    const inviteesErrorElement = document.getElementById('invitees-error');
    if (invitees === '') {
        displayError(inviteesErrorElement, 'Please select Invitees');
        return false;
    } else {
        hideError(inviteesErrorElement);
        return true;
    }
}

function validateCoordinates(i) {
    let isValid = true;
    if (isPlaceSelected(i)) {
        const latitudeInput = document.getElementById(`latitude-${i}`);
        const longitudeInput = document.getElementById(`longitude-${i}`);
        const inputElement = document.getElementById(`autocomplete-${i}`);
        const inputElementError = document.getElementById(`location-error-${i}`);

        if (!latitudeInput.value || !longitudeInput.value) {
            displayError(inputElementError, "Enter valid location")
            isValid = false;
        }

        const geocoder = new google.maps.Geocoder();
        const latLng = new google.maps.LatLng(parseFloat(latitudeInput.value), parseFloat(longitudeInput.value));

        geocoder.geocode({location: latLng}, function (results, status) {
            if (status === 'OK' && results[0]) {
                inputElement.value = results[0].formatted_address;
                isValid = true;
            } else {
                displayError(inputElementError, "Enter valid location")
                isValid = false;
            }
        });
    }
    return isValid;
}

function validateLink(i) {
    let isValid = true;
    if (isOnlineLinkSelected(i)) {
        const onlineLink = document.getElementById(`onlineLink-${i}`).value;
        const linkError = document.getElementById(`onlineLink-${i}-error`);

        try {
            const parsedUrl = new URL(onlineLink);
            if (parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:') {
                hideError(linkError);
                isValid = true;
            } else {
                displayError(linkError, "Invalid protocol. Use https.")
                isValid = false;
            }
        } catch (e) {
            displayError(linkError, "Invalid URL.")
            isValid = false;
        }
    }
    return isValid;
}

function generateEventPreview(data) {
    document.getElementById('eventTitle').textContent = data.title;
    document.getElementById('eventDate').textContent = `Created on: ${data.date}`;
    document.getElementById('eventStatus').textContent = data.isOpen ? 'Open' : 'Closed';
    document.getElementById('eventAuthor').textContent = `by ${data.author}`;
    document.getElementById('eventDescription').innerHTML = data.description;
    const carouselImages = document.getElementById('carouselImages');
    const carouselIndicators = document.getElementById('carouselIndicators');

    carouselImages.innerHTML = '';
    carouselIndicators.innerHTML = '';

    data.images.forEach((image, index) => {
        const carouselItem = document.createElement('div');
        carouselItem.className = `carousel-item ${index === 0 ? 'active' : ''}`;
        carouselItem.innerHTML = `<img src="${URL.createObjectURL(image)}" class="d-block w-100" alt="Event Image">`;
        carouselImages.appendChild(carouselItem);

        const indicator = document.createElement('li');
        indicator.setAttribute('data-bs-target', '#carouselExampleControls');
        indicator.setAttribute('data-bs-slide-to', index.toString());
        if (index === 0) {
            indicator.className = 'active';
        }
        carouselIndicators.appendChild(indicator);
    });


    const eventDetails = document.getElementById('eventDetails');
    eventDetails.innerHTML = '';
    data.daysInfos.forEach((day, index) => {
        const listItem = document.createElement('li');
        listItem.innerHTML = `
                    <h5 class="mt-3">Day ${index + 1}</h5>
                    <p><strong>Date:</strong> ${day.date}</p>
                    <p><strong>Time:</strong> ${day.startTime} - ${day.finishTime}</p>
                    ${day.onlineLink ? `<p><a href="${day.onlineLink}" target="_blank">Online Link</a></p>` : ''}
                    ${day.addressLink ? `<p><a href="${day.addressLink}" target="_blank">Location Link</a></p>` : ''}
                `;
        eventDetails.appendChild(listItem);
    });

    const initiativeTypesContainer = document.getElementById('initiativeTypesContainer');
    initiativeTypesContainer.innerHTML = '';
    data.initiativeTypes.forEach(type => {
        const span = document.createElement('span');
        span.className = 'badge bg-success';
        span.textContent = type;
        initiativeTypesContainer.appendChild(span);
    });
}

function getEventPreviewData() {
    const title = document.getElementById("title").value;
    const description = quill.root.innerHTML;
    const isOpen = Boolean(document.getElementById('eventType').value);
    const images = imageUploader.currentFiles;
    const tags = Array.from(document.getElementById("initiatives").querySelectorAll('.form-check-input:checked')).map(tag => tag.value);
    const dayInfos = [];

    document.querySelectorAll('.dayInfo').forEach((dayInfo, index) => {
        const date = dayInfo.querySelector(`#date-${index}`).value;
        const startTime = dayInfo.querySelector(`#startTime-${index}`).value;
        const finishTime = dayInfo.querySelector(`#finishTime-${index}`).value;

        const locationCheckbox = dayInfo.querySelector(`#toggleLocation-${index}`);
        const onlineLinkCheckbox = dayInfo.querySelector(`#toggleOnlineLink-${index}`);

        const latitude = locationCheckbox.checked ? dayInfo.querySelector(`#latitude-${index}`).value : null;
        const longitude = locationCheckbox.checked ? dayInfo.querySelector(`#longitude-${index}`).value : null;
        const addressLink = locationCheckbox.checked ? `https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}` : null;
        const onlineLink = onlineLinkCheckbox.checked ? dayInfo.querySelector(`#onlineLink-${index}`).value : null;

        dayInfos.push({
            date: new Date(date).toLocaleString('en-US', {year: 'numeric', month: 'long', day: 'numeric'}),
            startTime: startTime,
            finishTime: finishTime,
            addressLink: addressLink,
            onlineLink: onlineLink,
        });
    });

    return {
        title: title,
        date: new Date().toLocaleString('en-US', {year: 'numeric', month: 'long', day: 'numeric'}),
        author: author,
        isOpen: isOpen,
        images: images,
        initiativeTypes: tags,
        daysInfos: dayInfos,
        description: description
    };
}

function displayError(element, message) {
    element.textContent = message;
    element.style.display = 'block';
}

function hideError(element) {
    element.textContent = ''
    element.style.display = 'none';
}
