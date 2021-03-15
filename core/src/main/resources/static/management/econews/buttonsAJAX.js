function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}
function clearAllTagsInTagList(){
    document.getElementById("tagsEdit").innerHTML='';
}

function sendDataFromSearchForm(){
    var url = "/management/eco-news?"
    var id = document.getElementById("idSearchForm").value;
    var author = document.getElementById("authorSearchForm").value;
    var title = document.getElementById("titleSearchForm").value;
    var text = document.getElementById("textSearchForm").value;
    var startDateS = document.getElementById("startDateSearchForm").value;
    var endDateS = document.getElementById("endDateSearchForm").value;
    var tag = document.getElementById("tagSearchForm").value;

    if (id!==""){
        url = url.concat("id="+id+"&");
    }
    if (author!==""){
        url = url.concat("author="+author+"&");
    }
    if (title!==""){
        url = url.concat("title="+title+"&");
    }
    if (text!==""){
        url = url.concat("text="+text+"&");
    }
    if (startDateS!==""){
        url = url.concat("startDate="+startDateS+"&");
    }
    if (endDateS!==""){
        url = url.concat("endDate="+endDateS+"&");
    }
    if (tag!==""){
        url = url.concat("tags="+tag+"&");
    }
    $.ajax({
        url: url,
        type: 'GET',
        success: function(res) {
            window.location.href= url;
        }
    });
}
function toggle(source) {
    var checkboxes = document.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i] != source)
            checkboxes[i].checked = source.checked;
    }
}

let checkedCh = 0;
function updateCheckBoxCount(chInt){
    let chBox = $('#checkbox' + chInt);
    let deleteBtn = $("#btnDelete");
    chBox.is(":checked") ? checkedCh++ : checkedCh--;
    if(checkedCh === 0) {
        deleteBtn.addClass("disabled");
    } else deleteBtn.removeClass("disabled");
}
function addBtnDisabled(){
     document.getElementById('submitAddBtn').disabled=true;
}
let formAddValid = false;
function addFormInputValidate(){
    var titleValid;
    var formData = $('#addEcoNewsForm').serializeArray().reduce(function (obj, item) {
        obj[item.name] = item.value;
        return obj;
    }, {});
     if(!(formData.title.length>1&&formData.title.length<170)){
         document.getElementById("errorModalSavetitle").innerText = "size must be between 1 and 170";
         titleValid = false;
     }else{
         titleValid = true;
     }
     var textValid;
     if (!(formData.text.length>20 && formData.text.length<63206)){
         document.getElementById("errorModalSavetext").innerText = "size must be between 20 and 63206";
         textValid = false;
     }else {
         textValid = true;
     }
     if((textValid==true)&&(titleValid==true)){
        formAddValid = true;
    }else{
         formAddValid = false;
    }
}
function tagClick(){
    var  tagsCheckBoxes = document.getElementsByClassName("tag-checkbox");
    let count = 0;
    for (var i=0;i<tagsCheckBoxes.length;i= i+1){
        if (tagsCheckBoxes.item(i).checked){
            count = count+1;
        }
    }
    var submitBtn =  document.getElementById("submitAddBtn");
    if (count==0||count>3){
        submitBtn.disabled = true;
    }else {
        submitBtn.disabled = false;
    }
    if(count===3){
        for (var i=0;i<tagsCheckBoxes.length;i= i+1){
            if (!tagsCheckBoxes.item(i).checked){
                tagsCheckBoxes.item(i).disabled = true;
            }
        }
    }else if (count<3){
        for (var i=0;i<tagsCheckBoxes.length;i = i+1){
            if (tagsCheckBoxes.item(i).disabled){
                tagsCheckBoxes.item(i).disabled = false;
            }
        }
    }
}
function countOfCheckedItem() {
    var  tagsCheckBoxes = document.getElementsByName('EditTags[]');
    let count = 0;
    for (var i=0;i<tagsCheckBoxes.length;i= i+1){
        if (tagsCheckBoxes.item(i).checked){
            count = count+1;
        }
    }
    if(count===3){
        for (var i=0;i<tagsCheckBoxes.length;i= i+1){
            if (!tagsCheckBoxes.item(i).checked){
                tagsCheckBoxes.item(i).disabled = true;
            }
        }
    }else if (count<3){
        for (var i=0;i<tagsCheckBoxes.length;i= i+1){
            if (tagsCheckBoxes.item(i).disabled){
                tagsCheckBoxes.item(i).disabled = false;
            }
        }
    }
}

function tagEditModalClick(){
    var  tagsCheckBoxes = document.getElementsByName('EditTags[]');
    let count = 0;
    for (var i=0;i<tagsCheckBoxes.length;i= i+1){
        if (tagsCheckBoxes.item(i).checked){
            count = count+1;
        }
    }
    var submitBtn =  document.getElementById("submitEditBtn");
    if (count==0||count>3){
        submitBtn.disabled = true;
    }else {
        submitBtn.disabled = false;
    }
    if(count===3){
        for (let i=0;i<tagsCheckBoxes.length;i= i+1){
            if (!tagsCheckBoxes.item(i).checked){
                tagsCheckBoxes.item(i).disabled = true;
            }
        }
    }else if (count<3){
        for (let i=0;i<tagsCheckBoxes.length;i= i+1){
            if (tagsCheckBoxes.item(i).disabled){
                tagsCheckBoxes.item(i).disabled = false;
            }
        }
    }

}

let formEditValid = false;
function editFormInputValidate(){
    var titleValid;
    var formData = $('#editEcoNewsForm').serializeArray().reduce(function (obj, item) {
        obj[item.name] = item.value;
        return obj;
    }, {});
    if(!(formData.title.length>1&&formData.title.length<170)){
        document.getElementById("errorModalUpdatetitle").innerText = "size must be between 1 and 170";
        titleValid = false;
    }else{
        titleValid = true;
    }
    var textValid;
    if (!(formData.text.length>20 && formData.text.length<63206)){
        document.getElementById("errorModalUpdatetext").innerText = "size must be between 20 and 63206";
        textValid = false;
    }else {
        textValid = true;
    }
    if((textValid==true)&&(titleValid==true)){
        formEditValid = true;
    }else{
        formEditValid = false;
    }
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
        let url = "/management/eco-news?query=";
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
        $("input:checked").each(function() {
            payload.tags.push($(this).val());
        });

        var result = new FormData();
        result.append("addEcoNewsDtoRequest", new Blob([JSON.stringify(payload)], {type: "application/json"}));
        var file = document.getElementById("creationFile").files[0];
        result.append("file", file);
        addFormInputValidate()
        if(formAddValid==true){
            //save request in addEcoNewsModal
            document.getElementById("submitAddBtn").disabled=true;
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
                        // location.reload();
                        window.location.href = "/management/eco-news"
                    }
                },
                data: result
            });
        }

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

            $.get("/management/eco-news/tags",function (allTags,status){
                let tagsContainer = document.createElement('div');

                var tags = econews.tags;

                allTags.forEach(item=>{
                    var box = document.createElement('div');
                    box.classList.add("custom-checkbox");
                    var checkBoxstatus = '';
                    tags.forEach(t=>{
                        if (t===item.name){
                            checkBoxstatus = "checked";
                        }
                    })
                    box.innerHTML=`<span class="modal-checkbox">
                <input onclick="tagEditModalClick()" type="checkbox" value="${item.name}"  id="checkboxEditTag1" name="EditTags[]" ${checkBoxstatus}>
                <label for="checkboxTag1">${item.name}</label>
                <span>
                `;
                    document.querySelector('#tagsEdit').appendChild(box);
                });
                countOfCheckedItem();
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
        tagList.forEach(i=>{
            if (i.checked){
                returnData.tags.push(i.value);
            }
        });
        var result = new FormData();
        result.append("ecoNewsDtoManagement", new Blob([JSON.stringify(returnData)], {type: "application/json"}));
        var file = document.getElementById("file").files[0];
        result.append("file", file);
        //save request in editEcoNewsModal
        editFormInputValidate();
        if (formEditValid ==true){
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
});

function markCurentPageOnNav(){
    document.getElementById("eco-news-nav").classList.add("eco-news-active-link");
}

function orderByNameField(nameField){
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    var page = urlSearch.get("page");
    if (page!==null){
        urlSearch.set("page","0");
    }
    if (sort==null){
        urlSearch.set("sort",nameField+",ASC");
    }
    else if (sort.includes(nameField)) {
        sort = sort.toUpperCase();
        if (sort.includes("ASC")){
            urlSearch.set("sort",nameField+",DESC");
        }
        else if(sort.includes("DESC")) {
            urlSearch.set("sort",nameField+',ASC');
        }
    }else {
        urlSearch.set("sort",nameField+",ASC");
    }

    let url = "/management/eco-news?";
    $.ajax({
        url: url + urlSearch.toString(),
        type: 'GET',
        success: function(res) {
            window.location.href= url + urlSearch.toString();
        }
    });
}
// mark order
function markOrder(){
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var sort = urlSearch.get("sort");
    if (sort!==null){
        if(sort.includes('id')){
            if (sort.includes('ASC')){
                document.getElementById("id-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("id-icon").className="fas fa-chevron-down";
            }
        }else if(sort.includes('author.name')){
            if (sort.includes('ASC')){
                document.getElementById("author-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("author-icon").className="fas fa-chevron-down";
            }
        }else if(sort.includes('title')){
            if (sort.includes('ASC')){
                document.getElementById("title-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("title-icon").className="fas fa-chevron-down";
            }
        }else if (sort.includes('text')){
            if (sort.includes('ASC')){
                document.getElementById("text-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("text-icon").className="fas fa-chevron-down";
            }
        }else if(sort.includes('creationDate')){
            if (sort.includes('ASC')){
                document.getElementById("date-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("date-icon").className="fas fa-chevron-down";
            }
        }else if(sort.includes('tags')){
            if (sort.includes('ASC')){
                document.getElementById("tags-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("tags-icon").className="fas fa-chevron-down";
            }
        }else if(sort.includes('usersLikedNews')){
            if (sort.includes('ASC')){
                document.getElementById("likes-icon").className='fas fa-chevron-up';
            }else {
                document.getElementById("likes-icon").className="fas fa-chevron-down";
            }
        }
    }
}

//sidebar
function openNav() {
    document.getElementById("mySidepanel").style.width = "250px";
    document.getElementById("openbtnId").hidden = true;
    document.getElementById("tab-content").style.marginLeft="15%";
    // document.getElementById("eco-news-content").style.marginRight="15%";
}

function closeNav() {
    document.getElementById("mySidepanel").style.width = "0";
    document.getElementById("openbtnId").hidden = false;
    document.getElementById("tab-content").style.marginLeft="0";
    // document.getElementById("eco-news-content").style.marginRight="0";
}
