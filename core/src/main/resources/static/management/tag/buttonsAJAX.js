function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

let checkedCh = 0;

$(document).ready(function () {
    let deleteBtn = $("#btnDelete");
    console.log('js...');

    $('[data-toggle="tooltip"]').tooltip();

    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function() {
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
    checkbox.click(function(){
        if(!this.checked){
            $("#selectAll").prop("checked", false);
        }
    });

    $('#btnSearchImage').click(function (){
        let url = "/management/tags?filter=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function(res) {
                window.location.href= url + query;
            }
        });
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

    function sendAjaxPostRequest(payload) {
        $.ajax({
            url: '/management/tags',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        console.log(el.fieldName);
                        $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(payload)
        });
    }

    function sendAjaxPutRequest(payload, tagId) {
        $.ajax({
            url: `/management/tags/${tagId}`,
            type: 'put',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(payload)
        });
    }

    function getCheckedValues(checkbox) {
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(parseInt(this.value));
            }
        });

        return payload;
    }

    function getDataFromForm(formId) {
        return $(formId).serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
    }

    function getTranslationsFromFormData(formData) {
        var translations = [];
        for (var key in formData) {
            if (key.startsWith("name")) {
                var nameAndLanguage = key.split("_");
                var langId = nameAndLanguage[1];
                var langCode = nameAndLanguage[2];
                var name = formData["name_" + langId + "_" + langCode];
                translations.push({
                    "name": name,
                    "language": {
                        "code": langCode,
                        "id": langId
                    }
                });
            }
        }

        return translations;
    }

    function composePayloadFromFormData(formData) {
        return {
            "type": formData.tagType,
            "tagTranslations": getTranslationsFromFormData(formData)
        };
    }

    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editTagModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
        clearAllErrorsSpan();
        $('#editTagModal').modal();
        var href = $(this).attr('href');
        $.get(href, function (tag) {
            $('#id').val(tag.id);
            $('#tagType').val(tag.type);
            tag.tagTranslations.forEach(function (translation) {
                $(`#name_${translation.languageVO.id}_${translation.languageVO.code}`)
                    .val(translation.name);
            })
        });
    });

    $('#addTagModalBtn').on('click', function () {
        clearAllErrorsSpan();
    });

    $('td .delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deleteTagModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });

    $('#deleteOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        sendAjaxDeleteRequest(href);
    });

    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var href = '/management/tags';
        var payload = getCheckedValues(checkbox);
        sendAjaxDeleteRequest(href, payload);
    });

    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = getDataFromForm('#addTagForm');
        var payload = composePayloadFromFormData(formData);
        sendAjaxPostRequest(payload);
    });

    $('#submitEditBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = getDataFromForm('#editTagForm');
        var tagId = formData.id;
        var payload = composePayloadFromFormData(formData);
        sendAjaxPutRequest(payload, tagId);
    })
});