function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

$(document).ready(function () {
    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes
    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function () {
        if (this.checked) {
            checkbox.each(function () {
                this.checked = true;
            });
        } else {
            checkbox.each(function () {
                this.checked = false;
            });
        }
    });
    checkbox.click(function () {
        if (!this.checked) {
            $("#selectAll").prop("checked", false);
        }
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
        $('#deleteEcoNewsModal').modal();
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
            "image": formData.image,
            "source": formData.source,
            "tags": []
        };
        for (var key in formData) {

            if (key.startsWith("tags") && formData["tags"].trim().length !== 0) {
                var tag = formData["tags"].toString().split(/[\s,]+/);

                for (var i = 0; i < tag.length; i++) {
                    payload.tags.push(tag[i]);
                }
            }
        }
        var result = new FormData();
        result.append("addEcoNewsDtoRequest", new Blob([JSON.stringify(payload)], {type: "application/json"}));
        var file = document.getElementById("creationFile").files[0];
        result.append("file", file);
        //save request in addEcoNewsModal
        $.ajax({
            url: '/management/eco-news/',
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
                    location.reload();
                }
            },
            data: result
        });
    });

    //edit button on the right in the table
    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editEcoNewsModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
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
        for (var key in formData) {
            if (key.startsWith("tags") && $("#" + key).val().trim().length !== 0) {

                var tag = $("#" + key).val().split(/[\s,]+/);

                for (var i = 0; i < tag.length; i++) {
                    returnData.tags.push(tag[i]);
                }
            }
        }
        var result = new FormData();
        result.append("ecoNewsDtoManagement", new Blob([JSON.stringify(returnData)], {type: "application/json"}));
        var file = document.getElementById("file").files[0];
        result.append("file", file);
        //save request in editEcoNewsModal
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
    })

});
