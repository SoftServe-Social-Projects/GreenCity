let checkedChRemove = 0;
function updateCheckBoxCountRemove(chInt){
    let chBox = $('#checkbox' + chInt);
    let deleteBtn = $("#btnDelete");
    chBox.is(":checked") ? checkedChRemove++ : checkedChRemove--;
    console.log(checkedChRemove)
    if(checkedChRemove === 0) {
        deleteBtn.addClass("disabled");
    } else deleteBtn.removeClass("disabled");
}

let checkedChAdd = 0;
function updateCheckBoxCountAdd(chInt){
    let chBox = $('#checkboxAdd' + chInt);
    let addBtn = $("#btnAdd");
    chBox.is(":checked") ? checkedChAdd++ : checkedChAdd--;
    console.log(checkedChAdd)
    if(checkedChAdd === 0) {
        addBtn.addClass("disabled");
    } else addBtn.removeClass("disabled");
}


$(document).ready(function () {
    let deleteBtn = $("#btnDelete");

    // Activate tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Select/Deselect checkboxes remove
    var checkboxRemove = $('.remove-checkbox');
    $("#selectAllRemove").click(function(){
        if(this.checked){
            checkedChRemove = 0;
            checkboxRemove.each(function(){
                this.checked = true;
                checkedChRemove++;
            });
            deleteBtn.removeClass("disabled");
        } else{
            checkboxRemove.each(function(){
                checkedChRemove--;
                this.checked = false;
            });
            deleteBtn.addClass("disabled");
        }
    });

    checkboxRemove.click(function(){
        if(!this.checked){
            $("#selectAllRemove").prop("checked", false);
        }
    });
    var allParam = window.location.search;
    var urlSearch = new URLSearchParams(allParam);
    var habitId = urlSearch.get("habitId");

    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var payload = [];
        checkboxRemove.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        });
        var href = '/management/habit-to-do-list/deleteAll/?habitId='+habitId;
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


    /// add all
    let addBtn = $("#btnAdd");
    var checkboxAdd = $('.add-checkbox');
    $('#selectAllAdd').click(function (){
        if(this.checked){
            checkedChAdd = 0;
            checkboxAdd.each(function(){
                this.checked = true;
                checkedChAdd++;
            });
            addBtn.removeClass("disabled");
        } else{
            checkboxAdd.each(function(){
                checkedChAdd--;
                this.checked = false;
            });
            addBtn.addClass("disabled");
        }
    });
    checkboxAdd.click(function(){
        if(!this.checked){
            $("#selectAllAdd").prop("checked", false);
        }
    });

    $('#addAllSubmit').on('click', function (event) {
        event.preventDefault();
        var payload = [];
        checkboxAdd.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        });
        var href = '/management/habit-to-do-list/addAll/?habitId='+habitId;
        $.ajax({
            url: href,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            },
            data: JSON.stringify(payload)
        });
    });
//delete button in deleteItemModal
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
        $('#deleteToDoListItemModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });

    //add button in addItemModal
    $('#addOneSubmit').on('click', function (event) {
        event.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            type: 'post',
            dataType: 'json',
            contentType: 'application/json',
            success: function (data) {
                location.reload();
            }
        });
    });
    //add button on the right in the table
    $('.add.addBtn').on('click', function (event) {
        event.preventDefault();
        $('#addToDoListItemModal').modal();
        var href = $(this).attr('href');
        $('#addOneSubmit').attr('href', href);
    });

});

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
