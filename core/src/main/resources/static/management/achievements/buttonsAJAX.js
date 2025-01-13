function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

let checkedCh = 0;
function updateCheckBoxCount(chInt){
    let chBox = $('#checkbox' + chInt);
    let deleteBtn = $("#btnDelete");
    chBox.is(":checked") ? checkedCh++ : checkedCh--;
    console.log(checkedCh)
    if(checkedCh === 0) {
        deleteBtn.addClass("disabled");
    } else deleteBtn.removeClass("disabled");
}

$(document).ready(function () {
    let deleteBtn = $("#btnDelete");

    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes
    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function(){
        if(this.checked){
            checkedCh = 0;
            checkbox.each(function(){
                this.checked = true;
                checkedCh++;
            });
            deleteBtn.removeClass("disabled");
        } else{
            checkbox.each(function(){
                checkedCh--;
                this.checked = false;
            });
            deleteBtn.addClass("disabled");
        }
    });
    checkbox.click(function(){
        if(!this.checked){
            $("#selectAll").prop("checked", false);
        }
    });

    $('#btnSearchImage').click(function (){
        let url = "/management/achievement?query=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function(res) {
                window.location.href= url + query;
            }
        });
    });

    //delete button in deleteEcoNewsModal
    $('#deleteOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            type: 'delete',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            }
        });
    });

    //delete button on the right in the table
    $('td .delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deleteAchievementModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });

    //delete button in deleteAllSelectedModal
    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var checkbox = $('table tbody input[type="checkbox"]');
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        });
        var href = '/management/achievement/deleteAll';
        $.ajax({
            url: href,
            type: 'delete',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    });

    // submit button in addAchievementModal
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();

        var formData = $('#addAchievementForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});

        var payload = {
            "title": formData.title,
            "name": formData.name,
            "nameEng": formData.nameEng,
            "achievementCategory": {
                "name": formData.achievementCategory
            },
            "condition": formData.condition
        };

        // AJAX request to save achievement
        $.ajax({
            url: '/management/achievement',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                    });
                } else {
                    location.reload();
                }
            }
        });
    });

// edit button on the right in the table
    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editAchievementModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
        clearAllErrorsSpan();
        $('#editAchievementModal').modal();
        var $row = $(this).closest('tr');

        var achievementId = $row.find('td:nth-child(2)').text();
        var title = $row.find('td:nth-child(3)').text();
        var nameUa = $row.find('td:nth-child(4) span:nth-child(1)').text();
        var nameEng = $row.find('td:nth-child(4) span:nth-child(3)').text();
        var achievementCategory = $row.find('td:nth-child(5)').text();
        var condition = $row.find('td:nth-child(6)').text();

        $('#id').val(achievementId);
        $('input[name="title"]').val(title);
        $('input[name="name"]').val(nameUa);
        $('input[name="nameEng"]').val(nameEng);
        $('input[name="condition"]').val(condition);
        $('select[name="achievementCategory"]').val(achievementCategory.trim());
    });

// Submit button in the editAchievementModal
    $('#submitEditBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();

        var formData = $('#editAchievementForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var payload = {
            "id": formData.id,
            "title": formData.title,
            "name": formData.name,
            "nameEng": formData.nameEng,
            "achievementCategory": {
                "name": formData.achievementCategory
            },
            "condition": formData.condition
        };

        // Send the AJAX request to update the achievement
        $.ajax({
            url: '/management/achievement',
            type: 'PUT',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError);
                    });
                } else {
                    location.reload();
                }
            }
        });
    });
 });

function orderByField(fieldName = null, sortOrder = null) {
    let urlSearch = new URLSearchParams(window.location.search);
    let sortParams = [];
    const baseUrl = document.body.getAttribute('data-base-url');
    urlSearch.forEach((value, key) => {
        if (key === "sort" && value !== "") {
            sortParams.push(value);
        }
    });

    if (fieldName && sortOrder) {
        let updated = false;

        for (let i = 0; i < sortParams.length; i++) {
            let [field, order] = sortParams[i].split(',');
            if (field === fieldName) {
                if (order === sortOrder) {
                    sortParams.splice(i, 1);
                } else {
                    sortParams[i] = `${fieldName},${sortOrder}`;
                }
                updated = true;
                break;
            }
        }

        if (!updated) {
            sortParams.push(`${fieldName},${sortOrder}`);
        }

        urlSearch.delete("sort");
        sortParams.forEach(param => urlSearch.append("sort", param));

        const url = baseUrl + `?${urlSearch.toString()}`;
        $.ajax({
            url: url,
            type: 'GET',
            success: function () {
                window.location.href = url;
            }
        });
    }

    const fieldMapping = {
        'id': ['id-icon-asc', 'id-icon-desc'],
        'title': ['title-icon-asc', 'title-icon-desc'],
        'name': ['name-icon-asc', 'name-icon-desc'],
        'achievementCategory.name': ['category-icon-asc', 'category-icon-desc'],
        'condition': ['condition-icon-asc', 'condition-icon-desc']
    };

    sortParams.forEach(param => {
        let [field, order] = param.split(',');
        const icons = fieldMapping[field];
        if (icons) {
            const direction = order.toLowerCase() === 'asc' ? 0 : 1;
            document.getElementById(icons[direction]).style.color = 'green';
        }
    });

    updatePaginationLinks(urlSearch);
}

function updatePaginationLinks(urlSearch) {
    const paginationLinks = document.querySelectorAll('.pagination a.page-link');

    paginationLinks.forEach(link => {
        const url = new URL(link.getAttribute('href'), window.location.origin);
        const pageParam = url.searchParams.get('page');
        const queryParam = url.searchParams.get('query');

        urlSearch.delete('page');
        urlSearch.delete('query');

        url.search = urlSearch.toString();

        if (pageParam) {
            url.searchParams.set('page', pageParam);
        }
        if (queryParam) {
            url.searchParams.set('query', queryParam);
        }

        link.setAttribute('href', url.toString());
    });
}

document.addEventListener('DOMContentLoaded', function () {
    orderByField();
});