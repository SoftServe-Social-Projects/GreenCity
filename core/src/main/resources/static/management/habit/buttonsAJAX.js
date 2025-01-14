const link = '/management/habits/'

// Habit`s Facts

let checkedFactsIds = [];
let checkedChFacts = 0;

function updateCheckBoxCountFacts(chInt, id) {
    let chBox = $('#factscheckbox' + chInt);
    if (chBox.is(":checked")) {
        checkedChFacts++;
        checkedFactsIds.push(id);
    } else {
        checkedChFacts--;
        let pos = checkedFactsIds.indexOf(id)
        checkedFactsIds.splice(pos, 1)
    }

    let deactivateButton = $('#unlinktable1');
    if (checkedChFacts === 0) {
        deactivateButton.addClass("disabled");
    } else deactivateButton.removeClass("disabled");
}

function unlinkFacts(habitId) {
    clearAllErrorsSpan();

    // Ajax request
    $.ajax({
        url: '/management/facts/deleteAll',
        type: 'DELETE',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            location.reload();
        },
        data: JSON.stringify(checkedFactsIds)
    });
}

// Habit`s Advices

let checkedAdvicesIndexInList = [];
let checkedChAdvices = 0;

function updateCheckBoxCountAdvices(chInt) {
    let chBox = $('#advicescheckbox' + chInt);
    if (chBox.is(":checked")) {
        checkedChAdvices++;
        checkedAdvicesIndexInList.push(chInt);
    } else {
        checkedChAdvices--;
        let pos = checkedAdvicesIndexInList.indexOf(chInt)
        checkedAdvicesIndexInList.splice(pos, 1)
    }

    let deactivateButton = $('#unlinktable2');
    if (checkedChAdvices === 0) {
        deactivateButton.addClass("disabled");
    } else deactivateButton.removeClass("disabled");
}

function unlinkAdvices(habitId) {
    clearAllErrorsSpan();

    // Ajax request
    $.ajax({
        url: '/management/advices/' + habitId + '/unlink/advice',
        type: 'DELETE',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            location.reload();
        },
        data: JSON.stringify(checkedAdvicesIndexInList)
    });
}

// Habit`s To-Do List

let checkedToDoIds = [];
let checkedChToDo = 0;

function updateCheckBoxCountToDo(chInt, id) {
    let chBox = $('#ToDocheckbox' + chInt);
    if (chBox.is(":checked")) {
        checkedChToDo++;
        checkedToDoIds.push(id);
    } else {
        checkedChToDo--;
        let pos = checkedToDoIds.indexOf(id)
        checkedToDoIds.splice(pos, 1)
    }

    let deactivateButton = $('#unlinktable3');
    if (checkedChToDo === 0) {
        deactivateButton.addClass("disabled");
    } else deactivateButton.removeClass("disabled");
}


function unlinkToDo(habitId) {
    clearAllErrorsSpan();

    // Ajax request
    $.ajax({
        url: '/management/to-do-list-items/unlink/' + habitId,
        type: 'DELETE',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            location.reload();
        },
        data: JSON.stringify(checkedToDoIds)
    });
}

function deleteHabit(deleteUrl) {
    if (!confirm('Are you sure you want to delete this habit?')) {
        return;
    }

    $.ajax({
        url: deleteUrl,
        type: 'DELETE',
        success: function () {
            alert('Habit deleted successfully!');
            console.log('Habit successfully deleted');
            location.reload();
        },
        error: function (xhr) {
            console.error('Error deleting habit:', xhr.responseText);
            alert('Failed to delete habit. Please try again.');
        }
    });
}

$(document).ready(function () {
    $(document).on('click', '.eDelBtn', function (event) {
        event.preventDefault();
        const deleteUrl = $(this).attr('href');
        deleteHabit(deleteUrl);
    });
})


function linknew() {
    // TODO: create request
}

function sortByFieldName(nameField) {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    var page = urlSearch.get("page");
    if (page !== null) {
        urlSearch.set("page", "0");
    }
    if (sort === nameField + ",asc") {
        urlSearch.set("sort", nameField + ",desc");
        localStorage.setItem("sort", nameField + ",desc");
    } else {
        urlSearch.set("sort", nameField + ",asc");
        localStorage.setItem("sort", nameField + ",asc");
    }
    let url = "/management/habits?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}
