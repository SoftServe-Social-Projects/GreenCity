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
        let url = "/management/to-do-list-items?query=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function(res) {
                window.location.href= url + query;
            }
        });
    });

    //Кнопка edit справа в таблиці
    $('td .edit.eBtn').on('click', function (event) {
        event.preventDefault();
        $("#editToDoListItemModal").each(function () {
            $(this).find('input.eEdit').val("");
        });
        clearAllErrorsSpan();
        $('#editToDoListItemModal').modal();
        var href = $(this).attr('href');
        $.get(href, function (goal) {
            $('#id').val(goal.id);
            goal.translations.forEach(function (translation) {
                let lang = translation.language.code;
                $(`input[name=content${lang}]`).val(translation.content);
            });
        });
    });
    //Кнопка addGoal зверху таблиці
    $('#addToDoListItemModalBtn').on('click', function (event) {
        clearAllErrorsSpan();
    });
    //Кнопка delete справа в таблиці
    $('td .delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deleteToDoListItemModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });
    //Кнопка delete в deleteToDoListItemModal
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
            },
        });
    });
    //Кнопка delete в deleteAllSelectedModal
    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        });
        var href = '/management/to-do-list-items/deleteAll';
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
    //Кнопка submit в модальній формі Add
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#addToDoListItemForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var payload = {
            "toDoListItem": {
                "id": 1
            },
            "translations": []
        };
        for (var key in formData) {
            var lang;
            if (key.startsWith("content")) {
                lang = key.split("content").pop();
                payload.translations.push(
                    {
                        "content": formData["content" + lang],
                        "language": {
                            "code": lang,
                            "id": formData["id" + lang]
                        }
                    }
                );
            }
        }
        //запит save у модальній формі add
        $.ajax({
            url: '/management/to-do-list-items',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                    })
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(payload)
        });
    })

    //Кнопка submit в модальній формі Edit
    $('#submitEditBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#editToDoListItemForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var returnData = {
            "toDoListItem": {
                "id": formData.id
            },
            "translations": []
        };
        for (var key in formData) {
            var lang;
            if (key.startsWith("content")) {
                lang = key.split("content").pop();
                returnData.translations.push(
                    {
                        "content": formData["content" + lang],
                        "language": {
                            "code": lang,
                            "id": formData["id" + lang]
                        }
                    }
                );
            }
        }
        var href = '/management/to-do-list-items/' + returnData.id;
        //запит save у модальній формі update
        $.ajax({
            url: href,
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
            data: JSON.stringify(returnData)
        });
    })
});
