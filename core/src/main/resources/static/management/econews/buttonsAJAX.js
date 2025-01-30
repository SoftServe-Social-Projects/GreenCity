function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

function clearAllTagsInTagList() {
    document.getElementById("tagsEdit").innerHTML = '';
}

function toggle(source) {
    var checkboxes = document.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i] !== source)
            checkboxes[i].checked = source.checked;
    }
}

let checkedCh = 0;

function updateCheckBoxCount(chInt) {
    let chBox = $('#checkbox' + chInt);
    let deleteBtn = $("#btnDelete");
    chBox.is(":checked") ? checkedCh++ : checkedCh--;
    if (checkedCh === 0) {
        deleteBtn.addClass("disabled");
    } else deleteBtn.removeClass("disabled");
}

function addBtnDisabled() {
    document.getElementById('submitAddBtn').disabled = false;
}

let formAddValid = false;

function addFormInputValidate() {
    var titleValid;
    var formData = $('#addEcoNewsForm').serializeArray().reduce(function (obj, item) {
        obj[item.name] = item.value;
        return obj;
    }, {});
    if (!(formData.title.length > 1 && formData.title.length < 170)) {
        document.getElementById("errorModalSavetitle").innerText = "Size must be between 1 and 170";
        titleValid = false;
    } else {
        titleValid = true;
    }
    var textValid;
    if (!(formData.text.length > 20 && formData.text.length < 63206)) {
        document.getElementById("errorModalSavetext").innerText = "Size must be between 20 and 63206";
        textValid = false;
    } else {
        textValid = true;
    }
    var textValid;
    if (formData.source.length > 0 && !(formData.source.startsWith("http://")) && !(formData.source.startsWith("https://"))) {
        document.getElementById("errorModalSavesource").innerText = "Must start with http(s)://";
        textValid = false;
    } else {
        textValid = true;
    }
    if ((textValid === true) && (titleValid === true)) {
        formAddValid = true;
    }
}

//count tags for add
function tagClick() {
    var tagsCheckBoxes = document.getElementsByClassName("tag-checkbox");
    let count = 0;
    for (var i = 0; i < tagsCheckBoxes.length; i = i + 1) {
        if (tagsCheckBoxes.item(i).checked) {
            count = count + 1;
        }
    }
    var submitBtn = document.getElementById("submitAddBtn");
    submitBtn.disabled = (count === 0 || count > 3);
    if (count === 3) {
        for (var i = 0; i < tagsCheckBoxes.length; i = i + 1) {
            if (!tagsCheckBoxes.item(i).checked) {
                tagsCheckBoxes.item(i).disabled = true;
            }
        }
    } else if (count < 3) {
        for (var i = 0; i < tagsCheckBoxes.length; i = i + 1) {
            if (tagsCheckBoxes.item(i).disabled) {
                tagsCheckBoxes.item(i).disabled = false;
            }
        }
    }
}

function editBtnDisabled() {
    document.getElementById('submitEditBtn').disabled = true;
}

//count tags for edit
function tagEditClick() {
    var tagsCheckBoxes = document.getElementsByClassName("tag-checkbox");
    let count = 0;
    for (var i = 0; i < tagsCheckBoxes.length; i = i + 1) {
        if (tagsCheckBoxes.item(i).checked) {
            count = count + 1;
        }
    }
    var submitBtn = document.getElementById("submitEditBtn");
    submitBtn.disabled = (count === 0 || count > 3);
    if (count === 3) {
        for (let i = 0; i < tagsCheckBoxes.length; i = i + 1) {
            if (!tagsCheckBoxes.item(i).checked) {
                tagsCheckBoxes.item(i).disabled = true;
            }
        }
    } else if (count < 3) {
        for (let i = 0; i < tagsCheckBoxes.length; i = i + 1) {
            if (tagsCheckBoxes.item(i).disabled) {
                tagsCheckBoxes.item(i).disabled = false;
            }
        }
    }
}


let formEditValid = false;

function editFormInputValidate() {
    var titleValid;
    var formData = $('#editEcoNewsForm').serializeArray().reduce(function (obj, item) {
        obj[item.name] = item.value;
        return obj;
    }, {});
    if (!(formData.title.length > 1 && formData.title.length < 170)) {
        document.getElementById("errorModalUpdatetitle").innerText = "Size must be between 1 and 170";
        titleValid = false;
    } else {
        titleValid = true;
    }
    var textValid;
    if (!(formData.text.length > 20 && formData.text.length < 63206)) {
        document.getElementById("errorModalUpdatetext").innerText = "Size must be between 20 and 63206";
        textValid = false;
    } else {
        textValid = true;
    }
    var textValid;
    if (formData.source.length > 0 && !(formData.source.startsWith("http://")) && !(formData.source.startsWith("https://"))) {
        document.getElementById("errorModalUpdatesource").innerText = "Must start with http(s)://";
        textValid = false;
    } else {
        textValid = true;
    }
    if ((textValid === true) && (titleValid === true) && (titleValid === true)) {
        formEditValid = true;
    }
}


$(document).ready(function () {
    let deleteBtn = $("#btnDelete");

    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes
    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function () {
        if (this.checked) {
            checkedCh = 0;
            checkbox.each(function () {
                this.checked = true;
                checkedCh++;
            });
            deleteBtn.removeClass("disabled");
        } else {
            checkbox.each(function () {
                checkedCh--;
                this.checked = false;
            });
            deleteBtn.addClass("disabled");
        }
    });
    checkbox.click(function () {
        if (!this.checked) {
            $("#selectAll").prop("checked", false);
        }
    });

    $('#btnSearchImage').click(function () {
        let url = "/management/eco-news?query=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function (res) {
                window.location.href = url + query;
            }
        });
    });

    //delete button in deleteEcoNewsModal
    $('#deleteOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        sendAjaxDeleteRequest(href);
    });

    function sendAjaxDeleteRequest(href, payload) {
        payload = payload !== undefined ? payload : {};
        $.ajax({
            url: href,
            type: 'delete',
            dataType: 'json',
            contentType: 'application/json',
            success: function () {
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    }

    //delete button on the right in the table
    $('.delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deleteEcoNewsModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });

    //delete button in deleteAllSelectedModal
    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var checkbox = $('input[type="checkbox"]');
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        });
        var href = '/management/eco-news/deleteAll';
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

    $('.edit.eHideBtn').on('click', function (event) {
        event.preventDefault();
        $('#hideEcoNewsModal').modal();
        let href = $(this).attr('href');
        $('#hideOneSubmit').attr('href', href);
    });

    $('#hideOneSubmit').on('click', function (event) {
        event.preventDefault();
        let href = $(this).attr('href');
        sendAjaxPatchRequest(href);
    });

    $('.edit.eShowBtn').on('click', function (event) {
        event.preventDefault();
        $('#showEcoNewsModal').modal();
        let href = $(this).attr('href');
        $('#showOneSubmit').attr('href', href);
    });

    $('#showOneSubmit').on('click', function (event) {
        event.preventDefault();
        let href = $(this).attr('href');
        sendAjaxPatchRequest(href);
    });

    function sendAjaxPatchRequest(href, payload) {
        payload = payload !== undefined ? payload : {};
        $.ajax({
            url: href,
            type: 'patch',
            dataType: 'json',
            contentType: 'application/json',
            success: function () {
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    }

    //add EcoNews button at the top
    $('#addEcoNewsModalBtn').on('click', function (event) {
        clearAllErrorsSpan();
    });

    //submit button in addEcoNewsModal
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#addEcoNewsForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var payload = {
            "id": formData.id,
            "title": formData.title,
            "text": formData.text,
            "image": formData.imagePath,
            "source": formData.source,
            "tags": []
        };
        console.log(payload);
        $("input:checked").each(function () {
            payload.tags.push($(this).val());
        });

        var result = new FormData();
        result.append("addEcoNewsDtoRequest", new Blob([JSON.stringify(payload)], {type: "application/json"}));
        var file = document.getElementById("creationFile").files[0];
        console.log(file);
        result.append("image", file);
        addFormInputValidate()
        if (formAddValid === true) {
            //save request in addEcoNewsModal
            document.getElementById("submitAddBtn").disabled = false;
            $.ajax({
                url: '/management/eco-news/save',
                type: 'post',
                contentType: false,
                processData: false,
                dataType: "json",
                cache: false,
                success: function (data) {
                    if (Array.isArray(data.errors) && data.errors.length) {
                        data.errors.forEach(function (el) {
                            $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                        })
                    } else {
                        // location.reload();
                        window.location.href = "/management/eco-news";

                    }
                },
                data: result
            });
        }

    });

    //generate EcoNews content button in addEcoNewsModal
    $('#toggleGenerateEcoNewsForm').on('click', function (event) {
        const form = document.getElementById('generateEcoNewsForm');
        form.style.display = form.style.display === 'none' ? 'block' : 'none';
    });

    $('#generateEcoNewsContent').on('click', function (event) {
        const query = $('#generateQueryInput').val().trim();
        const language = localStorage.getItem("language") || "en";
        const locale = language === "ua" ? "uk-UA" : "en-US";

        $.ajax({
            url: '/ai/generate/eco-news',
            type: 'GET',
            data: { query: query },
            headers: { 'Accept-Language': locale },
            contentType: 'application/json',
            success: function (response) {
                console.log(response);
                const titleMatch = response.match(/\*\*Title:\s*(.+?)\s*\*\*/) || response.match(/\*\*\s*(.+?)\s*\*\*/);

                if (titleMatch) {
                    const title = titleMatch[1];
                    $('#inputTitle').val(title);

                    const updatedContent = response.replace(titleMatch[0], '').trim();

                    tinymce.get('ecoNewsContent').setContent(updatedContent);
                } else {
                    tinymce.get('ecoNewsContent').setContent(response);
                }
            },
            error: function (xhr, status, error) {
                console.error('Error generating Eco News content:', status, error);
                document.getElementById("errorModalGenerateContent").innerText =
                    'Failed to generate Eco News content. Please try again.';
            }
        });
    });

    //view users who liked/disliked modal
    $('.open-likesButton').on("click", function () {
        let newsId = $(this).data('id');
        $.ajax({
            type: 'GET',
            url: '/management/eco-news/' + newsId,
            dataType: 'json',
            success: function(data) {
                let parent = null;
                if(newsId.includes('dislikes')) {
                    parent = '.dislikes';
                } else {
                    parent = '.likes';
                }
                for (let i = 0; i < data.length; i++) {
                    let obj = data[i];
                    let newNode = $('<p></p>').addClass('modal-element').html(obj.name);
                    $(parent).append(newNode);
                }
            }
        });
    });

    // remove user likes data after closing modal
    $('#userLikesModal').on('hide.bs.modal', function () {
        let modalElements = document.querySelectorAll('.modal-element');
        modalElements.forEach(element => element.remove());
    });

    // remove user dislikes data after closing modal
    $('#userDislikesModal').on('hide.bs.modal', function () {
        let modalElements = document.querySelectorAll('.modal-element');
        modalElements.forEach(element => element.remove());
    });



    //edit button on the right in the table
    $('.edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editEcoNewsModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
        clearAllTagsInTagList();
        clearAllErrorsSpan();
        $('#editEcoNewsModal').modal();
        var href = $(this).attr('href');
        $.get(href, function (econews, status) {
            $('#id').val(econews.id);
            $('#title').val(econews.title);
            $('#text').val(econews.text);
            $('#imagePath').val(econews.imagePath);
            $('#source').val(econews.source);
            $('#tags').val(econews.tags);
            $('#file').val(econews.file);

            $.get("/management/eco-news/tags", function (allTags, status) {
                let tagsContainer = document.createElement('div');

                var tags = econews.tags;

                allTags.forEach(item => {
                    var box = document.createElement('div');
                    box.classList.add("custom-checkbox");
                    var checkBoxstatus = '';
                    tags.forEach(t => {
                        if (t === item.name) {
                            checkBoxstatus = "checked";
                        }
                    })
                    box.innerHTML = `<span class="modal-checkbox">
                <input onclick="tagEditClick()" type="checkbox" value="${item.name}"  id="checkboxEditTag1" name="EditTags[]" ${checkBoxstatus}>
                <label for="checkboxTag1">${item.name}</label>
                <span>
                `;
                    document.querySelector('#tagsEdit').appendChild(box);
                });
            });
        });
    });
    //submit button in editEcoNewsModal
    $('#submitEditBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#editEcoNewsForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var returnData = {
            "id": formData.id,
            "title": formData.title,
            "text": formData.text,
            "imagePath": formData.imagePath,
            "source": formData.source,
            "tags": []
        };
        var tagList = document.getElementsByName("EditTags[]");
        tagList.forEach(i => {
            if (i.checked) {
                returnData.tags.push(i.value);
            }
        });
        var result = new FormData();
        result.append("ecoNewsDtoManagement", new Blob([JSON.stringify(returnData)], {type: "application/json"}));
        var file = document.getElementById("fileUpdate").files[0];
        if (file) {
            result.append("file", file)
        }
        //save request in editEcoNewsModal
        editFormInputValidate();
        if (formEditValid) {
            $.ajax({
                url: '/management/eco-news/',
                type: 'put',
                contentType: false,
                processData: false,
                dataType: "json",
                cache: false,
                success: function (data) {
                    if (Array.isArray(data.errors) && data.errors.length) {
                        data.errors.forEach(function (el) {
                            $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError);
                        })
                    } else {
                        location.reload();
                    }
                },
                data: result
            });
        }
    })

    //Include Date Range Picker
    $('.input-daterange input').each(function () {
        $(this).datepicker({
            format: 'yyyy-mm-dd',
            todayHighlight: true,
            autoclose: true,
            orientation: 'top'
        });
    });

    $('.end-date').hide();

    $('.filter-container').hide();

    $('.eFilterBtn').on('click', function () {
        const $filterContainer = $(this).closest('th').find('.filter-container');

        $('.eFilterBtn').not(this).removeClass('active');
        $('.filter-container').not($filterContainer).hide();
        $(this).toggleClass('active');

        $filterContainer.toggle();
    });

    //Hide filter container when clicking outside of it
    $(document).on('click', function (e) {
        if (!$(e.target).closest('th').length) {
            $('.filter-container').hide();
            $('.eFilterBtn').removeClass('active');
        }
    });

    document.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            const target = event.target;

            if (target.id.startsWith('search-eco-news-by-')) {
                const fieldName = target.id.replace('search-eco-news-by-', '');
                searchByNameField(target.value, fieldName);
            }
        }
    });

    function isSortActive(fieldName, sortOrder) {
        let urlSearch = new URLSearchParams(window.location.search);
        let isActive = false;
        urlSearch.forEach((value, key) => {
            if (key === "sort") {
                let [field, order] = value.split(',');
                if (field === fieldName && order === sortOrder) {
                    isActive = true;
                }
            }
        });
        return isActive;
    }

    function isFilterActive(filterName) {
        let urlSearch = new URLSearchParams(window.location.search);
        return urlSearch.get(filterName) !== null && urlSearch.get(filterName) !== "";
    }

    $('.table-filter-icon').each(function() {
        let inputForm = $(this).closest('th').find('input.form-search');
        if (inputForm.length === 0) {
            inputForm = $(this).closest('th').find('input.form-control');
        }
        if (inputForm.length === 0) {
            inputForm = $(this).closest('th').find('div.custom-checkbox.tag');
        }
        if (inputForm.length !== 0) {
            const id = inputForm.attr('id');
            const filterField = id.split('-').pop();
            if (isFilterActive(filterField)) {
                $(this).addClass('filtered');
            }
        }
    });

    $('.sort-icon').each(function() {
        const field = $(this).data('field');
        const order = $(this).data('order');
        if (isSortActive(field, order)) {
            $(this).find('i').addClass('sorted');
        } else {
            $(this).find('i').removeClass('sorted');
        }
    });
});

// edit econew image
const loadFile = function (event) {
    const image = document.querySelector("#upload_image");
    const file = document.querySelector('#fileUpdate').files[0];
    let reader = new FileReader();
    reader.onloadend = () => {
        image.src = reader.result
    };
    if (file) {
        reader.readAsDataURL(file);
    }
};

function markCurentPageOnNav() {
    document.getElementById("eco-news-nav").classList.add("eco-news-active-link");
}

function removeFilter(filterName) {
    let urlSearch = getUrlSearchParams();
    urlSearch.delete(key);
    if (filterName === 'startDate') {
        urlSearch.delete('endDate');
    }

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function getSelectedRadioButton(radioGroupName) {
    const selectedRadio = document.querySelector(`input[name="${radioGroupName}"]:checked`);
    return selectedRadio ? selectedRadio.value : null;
}

function searchByQuery(query) {
    if (query === null || query === '') {
        removeFilter('query');
    }
    let urlSearch = getUrlSearchParams();
    urlSearch.set("query", query);

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function searchByNameField(searchValue, fieldName, searchValue2 = null, fieldName2 = null) {
    let urlSearch = getUrlSearchParams();
    if (searchValue !== null && searchValue !== "") {
        urlSearch.set(fieldName, searchValue);
    }
    if (searchValue2 !== null && searchValue2 !== "") {
        urlSearch.set(fieldName2, searchValue2);
    }

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function buildUrlPage(pageNumber) {
    console.log("page number:", pageNumber);
    let urlSearch = getUrlSearchParams();
    urlSearch.set("page", pageNumber);

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}

function orderByNameField(sortOrder, fieldName) {
    let urlSearch = getUrlSearchParams();
    let sortParams = [];
    urlSearch.forEach((value, key) => {
        if (key === "sort" && value !== "") {
            sortParams.push(value);
        }
    });

    let updated = false;
    for (let i = 0; i < sortParams.length; i++) {
        let [field, order] = sortParams[i].split(',');
        if (field === fieldName) {
            if (order === sortOrder) {
                sortParams.splice(i, 1);
            } else {
                sortParams[i] = fieldName + ',' + sortOrder;
            }
            updated = true;
            break;
        }
    }

    if (!updated) {
        sortParams.push(fieldName + ',' + sortOrder);
    }

    urlSearch.delete("sort");

    sortParams.forEach(param => {
        urlSearch.append("sort", param);
    });

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function (res) {
            window.location.href = url + urlSearch.toString();
        }
    });
}


// mark order
function markOrder() {
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    if (sort !== null) {
        if (sort.includes('id')) {
            if (sort.includes('ASC')) {
                document.getElementById("id-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("id-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('author.name')) {
            if (sort.includes('ASC')) {
                document.getElementById("author-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("author-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('title')) {
            if (sort.includes('ASC')) {
                document.getElementById("title-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("title-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('text')) {
            if (sort.includes('ASC')) {
                document.getElementById("text-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("text-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('creationDate')) {
            if (sort.includes('ASC')) {
                document.getElementById("date-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("date-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('tags')) {
            if (sort.includes('ASC')) {
                document.getElementById("tags-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("tags-icon").className = "fas fa-chevron-down";
            }
        } else if (sort.includes('usersLikedNews')) {
            if (sort.includes('ASC')) {
                document.getElementById("likes-icon").className = 'fas fa-chevron-up';
            } else {
                document.getElementById("likes-icon").className = "fas fa-chevron-down";
            }
        }
    }
}

//sidebar
function openNav() {
    document.getElementById("mySidepanel").style.width = "250px";
    document.getElementById("openbtnId").hidden = true;
    document.getElementById("tab-content").style.marginLeft = "15%";
    // document.getElementById("eco-news-content").style.marginRight="15%";
}

function closeNav() {
    document.getElementById("mySidepanel").style.width = "0";
    document.getElementById("openbtnId").hidden = false;
    document.getElementById("tab-content").style.marginLeft = "0";
    // document.getElementById("eco-news-content").style.marginRight="0";
}

document.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        const target = event.target;

        if (target.id.startsWith('search-eco-news-by-')) {
            event.preventDefault(); // Prevent form submission
            const fieldName = target.id.replace('search-eco-news-by-', '');
            searchByNameField(target.value, fieldName);
        }
    }
});

function applyTagFilter() {
    let selectedTags = [];

    $('input[name="tags"]:checked').each(function() {
        selectedTags.push($(this).val());
    });

    let tagValues = selectedTags.join(',');

    searchByNameField(tagValues, 'tags');
}

function toggleEndDate() {
    $('.end-date').toggle();
}

function getUrlSearchParams() {
    let allParam = window.location.search;
    let urlSearch = new URLSearchParams(allParam);

    let sortParams = [];
    urlSearch.forEach((value, key) => {
        if (key === "sort") {
            sortParams.push(value);
        }
    });

    let params = {
        id: urlSearch.get("id"),
        author: urlSearch.get("author"),
        title: urlSearch.get("title"),
        text: urlSearch.get("text"),
        startDate: urlSearch.get("startDate"),
        endDate: urlSearch.get("endDate"),
        tags: urlSearch.get("tags"),
        hidden: urlSearch.get("hidden"),
        query: urlSearch.get("query"),
        page: urlSearch.get("page")
    };

    if (params.page !== null) {
        urlSearch.set("page", "0");
    }

    urlSearch = new URLSearchParams();

    for (let key in params) {
        if (params[key] !== null) {
            urlSearch.set(key, params[key]);
        }
    }

    sortParams.forEach(param => {
        urlSearch.append("sort", param);
    });

    return urlSearch;
}

function isSortActive(fieldName, sortOrder) {
    let urlSearch = getUrlSearchParams();
    let isActive = false;
    urlSearch.forEach((value, key) => {
        if (key === "sort") {
            let [field, order] = value.split(',');
            if (field === fieldName && order === sortOrder) {
                isActive = true;
            }
        }
    });
    return isActive;
}

function isFilterActive(filterName) {
    let urlSearch = getUrlSearchParams();
    return urlSearch.get(filterName) !== null && urlSearch.get(filterName) !== "";
}