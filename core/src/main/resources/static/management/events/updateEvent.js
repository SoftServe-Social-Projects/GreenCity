"use strict"

const description = quill.clipboard.convert(eventDto.description);
quill.setContents(description, 'silent')
const previewImages = [eventDto.titleImage, ...eventDto.additionalImages]
// Get all images to preview
Promise.all(previewImages.map(img => urlToFile(img, img.split("/").pop())))
    .then(files => {
        imageUploader.currentFiles = files;
        imageUploader.previewContainer.innerHTML = '';
        previewImages.forEach(img => imageUploader.addImageToPreview(img, img.split("/").pop()));
        imageUploader.updateFileList();
    })
    .catch(error => {
        console.error('Error converting URLs to files:', error);
        imageUploader.showError('Failed to load images.');
    })
eventDto.tags.forEach(tag => document.querySelector(`input[type="checkbox"][value="${tag.nameEn.toUpperCase()}"]`).checked = true)

document.getElementById("eventType").querySelector(`option[value="${eventDto.open}"]`).selected = true

durationSelect.value = eventDto.dates.length ?? 1;
generateDayInfoElements(daysInfoDiv, durationSelect.value);

eventDto.dates.forEach((date, i) => {
    initDate(date, i);
})

function extractDateAndTimes(startDate, finishDate) {
    const startDateObj = new Date(startDate);
    const finishDateObj = new Date(finishDate);

    const date = startDateObj.toISOString().split('T')[0];

    const startTime = startDateObj.toISOString().split('T')[1].slice(0, 5);
    const finishTime = finishDateObj.toISOString().split('T')[1].slice(0, 5);

    return {
        date,
        startTime,
        finishTime
    };
}


function submitUpdateEventForm() {
    const formData = createFormDataUpdate();
    fetch(`${backendAddress}/management/events`, {
        method: 'PUT',
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

function urlToFile(url, fileName) {
    return fetch(url)
        .then(response => response.blob())
        .then(blob => new File([blob], fileName, {type: blob.type}));
}

function createFormDataUpdate() {
    const title = document.getElementById("title").value;
    const description = quill.root.innerHTML;
    const tags = Array.from(document.getElementById("initiatives").querySelectorAll('.form-check-input:checked')).map(tag => tag.value);
    const isOpen = Boolean(document.getElementById('eventType').value);

    const eventDtoRequest = {
        title: title,
        description: description,
        tags: tags,
        open: isOpen,
        datesLocations: [],
        additionalImages: [],
        id: eventDto.id
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
        eventDtoRequest.datesLocations.push({
            startDate: startDateTime,
            finishDate: finishDateTime,
            ...locationData,
            ...onlineLinkData
        });
    });

    const formData = new FormData();
    formData.append('eventDto', new Blob([JSON.stringify(eventDtoRequest)], {type: 'application/json'}));

    const images = imageUploader.currentFiles;

    for (let i = 0; i < images.length; i++) {
        formData.append('images', images[i]);
    }
    return formData;
}

function initDate(date, i){
    const {date: dayDate, startTime, finishTime} = extractDateAndTimes(date.startDate, date.finishDate)
    const toggleLocationCheckbox = document.getElementById(`toggleLocation-${i}`);
    const toggleOnlineLinkCheckbox = document.getElementById(`toggleOnlineLink-${i}`);
    const onlineLinkFields = document.getElementById(`online-link-fields-${i}`);
    const locationFields = document.getElementById(`location-fields-${i}`);
    const autocomplete = document.getElementById(`autocomplete-${i}`);
    const latitudeInput = document.getElementById(`latitude-${i}`);
    const longitudeInput = document.getElementById(`longitude-${i}`);
    const onlineLinkField = document.getElementById(`onlineLink-${i}`);
    const allDayCheckBox = document.getElementById(`allDay-${i}`);
    const startTimeField = document.getElementById(`startTime-${i}`);
    const finishTimeField = document.getElementById(`finishTime-${i}`);
    const dateField = document.getElementById(`date-${i}`);
    const mapElement = document.getElementById(`map-${i}`);

    dateField.value = dayDate;
    startTimeField.value = startTime;
    finishTimeField.value = finishTime;
    if (startTime === "00:00" && finishTime === "23:59") {
        allDayCheckBox.checked = true
        startTimeField.disabled = true;
        finishTimeField.disabled = true;
    }

    if (date.coordinates) {
        toggleLocationCheckbox.checked = true
        locationFields.style.display = "block";
        autocomplete.value = date.coordinates.formattedAddressEn
        longitudeInput.value = date.coordinates.longitude
        latitudeInput.value = date.coordinates.latitude
        const marker = mapElement.marker;
        marker.setPosition({lat: date.coordinates.latitude, lng: date.coordinates.longitude})
    }

    if (date.onlineLink) {
        toggleOnlineLinkCheckbox.checked = true
        onlineLinkFields.style.display = "block"
        onlineLinkField.value = date.onlineLink
    }
}