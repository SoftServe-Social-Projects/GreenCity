var languages;
$.get('/factoftheday/languages',function (data){
    languages=data;
});

function clearAllErrorsSpan(){
    $('.errorSpan').text('');
}

$(document).ready(function(){
    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes
    var checkbox = $('table tbody input[type="checkbox"]');
    $("#selectAll").click(function(){
        if(this.checked){
            checkbox.each(function(){
                this.checked = true;
            });
        } else{
            checkbox.each(function(){
                this.checked = false;
            });
        }
    });
    checkbox.click(function(){
        if(!this.checked){
            $("#selectAll").prop("checked", false);
        }
    });
    //Кнопка edit справа в таблиці
    $('td .edit.eBtn').on('click', function(event) {
        event.preventDefault();
        $('#editFactOfTheDayModal').each(function() {
            $(this).find('input.eEdit').val("");
        });
        clearAllErrorsSpan();
        $('#editFactOfTheDayModal').modal();

        var $row = $(this).closest('tr');
        var id = $row.find('td.mobile-hidden:first').text().trim();
        var name = $row.find('td').eq(2).text().trim();
        var translations = $row.find('table.table-child tbody tr');

        $('#id').val(id);
        $('#name').val(name);

        translations.each(function() {
            var languageCode = $(this).find('td').eq(2).text().trim();
            var content = $(this).find('td').eq(1).text().trim();
            $('#content' + languageCode).val(content);
        });
        // Отримуємо теги, присвоєні цьому факту
        var assignedTags = [];
        $row.find('#tag-table-body tr').each(function() {
            var tagId = $(this).find('td').data('tag-id'); // Припустимо, що ти додаєш data-атрибут з tag-id
            assignedTags.push(tagId);
        });

        $('#id').val(id);
        $('#name').val(name);

        translations.each(function() {
            var languageCode = $(this).find('td').eq(2).text().trim();
            var content = $(this).find('td').eq(1).text().trim();
            $('#content' + languageCode).val(content);
        });

        loadTags('tagsContainerEdit', assignedTags); // Передаємо список тегів
    });
    //Кнопка addFactOftheDay зверху таблиці
    $('#addFactOfTheDayModalBtn').on('click',function(event){
        clearAllErrorsSpan();
        loadTags('tagsContainerAdd');
    });
    //Кнопка delete справа в таблиці
    $('td .delete.eDelBtn').on('click',function(event){
        event.preventDefault();
        $('#deleteFactOfTheDayModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href',href);
    });
    //Кнопка delete в deleteFactOfTheDayModal
    $('#deleteOneSubmit').on('click',function(event){
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
    $('#deleteAllSubmit').on('click',function(event){
        event.preventDefault();
        var payload=[];
        checkbox.each(function (){
            if(this.checked){
                payload.push(this.value);
            }
        })
        var href = '/management/factoftheday/deleteAll';
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
    // Кнопка submit в модальній формі Add
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#addFactOfTheDayForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var payload = {
            "id": formData.id,
            "name": formData.name,
            "factOfTheDayTranslations": [],
            "tags": []
        }
        for (var key in formData) {
            if (key.startsWith("content")) {
                var lang = key.split("content").pop();
                payload.factOfTheDayTranslations.push(
                    {
                        "content": formData["content" + lang],
                        "languageCode": lang
                    }
                );
            }
            if (key.startsWith("tag-")) {
                payload.tags.push(formData[key]);
            }
        }
        // Запит save у модальній формі add
        $.ajax({
            url: '/management/factoftheday/',
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function (el) {
                        $(document.getElementById('errorModalSave' + el.fieldName)).text(el.fieldError);
                    });
                } else {
                    location.reload();
                }
            },
            data: JSON.stringify(payload)
        });
    })

    //Кнопка submit в модальній формі Edit
    $('#submitEditBtn').on('click', function(event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#editFactOfTheDayForm').serializeArray().reduce(function(obj, item) {
            obj[item.name] = item.value;
            return obj;
        }, {});
        var returnData = {
            "id": formData.id,
            "name": formData.name,
            "factOfTheDayTranslations": [],
            "tags": []
        };

        // Збираємо переклади і теги
        for (var key in formData) {
            if (key.startsWith("content")) {
                var lang = key.split("content").pop();
                returnData.factOfTheDayTranslations.push({
                    "content": formData["content" + lang],
                    "languageCode": lang
                });
            }
        }

        $('input[type="checkbox"][name^="tag-"]:checked').each(function() {
            returnData.tags.push($(this).val());
        });
        //запит save у модальній формі update
        $.ajax({
            url: '/management/factoftheday/',
            type: 'put',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(returnData),
            success: function(data) {
                if (Array.isArray(data.errors) && data.errors.length) {
                    data.errors.forEach(function(el) {
                        $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError);
                    });
                } else {
                    location.reload(); // Оновлюємо сторінку після успішного збереження
                }
            }
        });
    })
});

// Завантаження тегів з сервера і додавання їх у форму (враховуючи код мови вибраний користувачем на сторінці)
function loadTags(tagsContainerId, assignedTags = []) {
    const currentLanguage = document.querySelector('.dropbtn.header').textContent.trim().toLowerCase();
    $.ajax({
        url: '/management/factoftheday/tags',
        type: 'get',
        dataType: 'json',
        success: function (tags) {
            var tagsContainer = $('#' + tagsContainerId);
            tagsContainer.empty();
            tags.forEach(function (tag) {
                if (tag.languageCode === currentLanguage) {
                    var isChecked = assignedTags.includes(tag.id) ? 'checked' : '';
                    tagsContainer.append(
                        '<div class="form-check">' +
                        '<input class="form-check-input" type="checkbox" value="' + tag.id + '" id="tag-' + tag.id + '" name="tag-' + tag.id + '" ' + isChecked + '>' +
                        '<label class="form-check-label" for="tag-' + tag.id + '">' + tag.name + '</label>' +
                        '</div>'
                    );
                }
            });
        }
    });
}

// Відображення tag в таблиці за кодом мови вибраним користувачем на сторінці
document.addEventListener('DOMContentLoaded', function() {
    const currentLanguage = document.querySelector('.dropbtn.header').textContent.trim().toLowerCase();
    setLanguage(currentLanguage);

    const languageLinks = document.querySelectorAll('.dropdown-content a');

    languageLinks.forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault();

            const selectedLanguage = link.textContent.trim().toLowerCase();
            setLanguage(selectedLanguage);
        });
    });
});

function setLanguage(selectedLanguage) {
    const rows = document.querySelectorAll('#tag-table-body td');

    rows.forEach(row => {
        const languageCode = row.getAttribute('data');
        row.style.display = (languageCode === selectedLanguage) ? '' : 'none';
    });
}

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
        'name': ['name-icon-asc', 'name-icon-desc'],
        'factOfTheDayTranslations.content': ['content-icon-asc', 'content-icon-desc'],
        'createDate': ['date-icon-asc', 'date-icon-desc']
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