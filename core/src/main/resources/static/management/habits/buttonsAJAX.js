function clearAllErrorsSpan() {
    $('.errorSpan').text('');
}

let checkedCh = 0;

function updateCheckBoxCount(chInt) {
    let chBox = $('#checkbox' + chInt);
    let deleteBtn = $("#btnDelete");
    chBox.is(":checked") ? checkedCh++ : checkedCh--;
    console.log(checkedCh)
    if (checkedCh === 0) {
        deleteBtn.addClass("disabled");
    } else deleteBtn.removeClass("disabled");
}

    /**
     * Trigering and submitting main search when 'Enter' button is pressed
     */
var searchInp = document.getElementById('habitSearch');
searchInp.addEventListener('keyup', function (event){
   if(event.keyCode === 13){
       event.preventDefault();
       $('.searching').submit();
   }
});

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
        let url = "/management/habits?query=";
        let query = $('#inputSearch').val();
        $.ajax({
            url: url + query,
            type: 'GET',
            success: function (res) {
                window.location.href = url + query;
            }
        });
    });
    //delete button on right side
    $('td .delete.eDelBtn').on('click', function (event) {
        event.preventDefault();
        $('#deleteHabitModal').modal();
        var href = $(this).attr('href');
        $('#deleteOneSubmit').attr('href', href);
    });
    //delete one button in modal
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
    //all delete button in modal
    $('#deleteAllSubmit').on('click', function (event) {
        event.preventDefault();
        var payload = [];
        checkbox.each(function () {
            if (this.checked) {
                payload.push(this.value);
            }
        })
        var href = '/management/habits/deleteAll';
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
    //add habit button at the top
    $('#addHabitModalBtn').on('click', function (event) {
        clearAllErrorsSpan();
    });
    //submit button in modal add
    $('#submitAddBtn').on('click', function (event) {
        event.preventDefault();
        clearAllErrorsSpan();
        var formData = $('#addHabitForm').serializeArray().reduce(function (obj, item) {
            obj[item.name] = item.value;
            console.log(obj);
            return obj;
        }, {});
        var returnData = {
            "id": formData.id,
            "image": formData.image,
            "defaultDuration": formData.defaultDuration,
            "complexity": formData.complexity,
            "habitTranslations": []

        }
        console.log(returnData);
        for (var key in formData) {
            if (key.startsWith("contentName") | key.startsWith("contentDescr") | key.startsWith("contentHabItem") | key.startsWith("complexity")| key.startsWith("defaultDuration")) {
                var lang, name, description, habitItem, complexity, defaultDuration;

                lang = key.slice(key.length - 2);
                if (key.startsWith("contentName")) {
                    name = formData["contentName" + lang]
                }
                if (key.startsWith("contentDescr")) {
                    description = formData["contentDescr" + lang]
                }
                if (key.startsWith("contentHabItem")) {
                    habitItem = formData["contentHabItem" + lang]
                }
                if (key.startsWith("complexity")) {
                    complexity = formData["complexity"]
                }
                if (key.startsWith("defaultDuration")) {
                    defaultDuration = formData["defaultDuration"]
                }
                if (name != null && description != null && habitItem != null && complexity != null && defaultDuration != null) {
                    returnData.habitTranslations.push(
                        {
                            "name": name,
                            "description": description,
                            "habitItem": habitItem,
                            "languageCode": lang

                        }
                    );
                    name = null;
                    description = null;
                    habitItem = null;

                }
                console.log(returnData);
            }
        }
        var result = new FormData();

        result.append("habitManagementDto", new Blob([JSON.stringify(returnData)], {type: "application/json"}));
        var file = document.getElementById("fileCreate").files[0];
        result.append("file", file);
        //request save in modal add
        $.ajax({
            url: '/management/habits/save',
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

    //edit button
    $('.table-edit-translation-icon').on('click', function (event) {
        event.preventDefault();

        clearAllErrorsSpan();
        $('#editHabitModal').modal();
        fetch(this.getAttribute(
            'href'
        ))
            .then(response => {

                let json = response.json();
                json.then((habit) => {

                    habit.habitTranslations.forEach(tr => {
                        const form = document.querySelector("#editHabitModal");

                        const input = form.querySelectorAll('.input_translation');
                        input.forEach(inp => {
                            if (inp.getAttribute('name').endsWith(tr.languageCode))
                                inp.setAttribute('value', tr.name);
                        })
                    })

                    $('#submitEditBtn').on('click', function (event) {
                        event.preventDefault();
                        clearAllErrorsSpan();
                        const formData = $('#editHabitForm').serializeArray().reduce(function (obj, item) {
                            obj[item.name] = item.value;
                            return obj;
                        }, {});
                        habit.habitTranslations.forEach(tr => {
                            for (const key in formData) {
                                const lang = key.slice(key.length - 2);
                                if (key.endsWith(tr.languageCode)) {
                                    tr.name = formData["contentName" + lang]
                                }
                            }
                        })
                        const hasErrors = data => {
                            data.errors.forEach(el =>
                                $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError));
                        }

                        const success = () => location.reload()

                        updateHabit(habit, null, success, hasErrors)
                    })
                })
            })
    })

    /**
     * Display habit complexity.
     //  */
    const habitsCompl = document.getElementsByClassName("habit_complexity");
    for (let i = 0; i < habitsCompl.length; i++) {
        const complexity = habitsCompl[i].getAttribute("data-complexity");
        for (let j = 0; j < complexity; j++) {
            const imgFill = document.createElement("img");
            imgFill.setAttribute("src", "/img/star-filled.png")
            habitsCompl[i].appendChild(imgFill);

        }
        for (let j = complexity; j < 3; j++) {
            const imgEmpty = document.createElement("img");
            imgEmpty.setAttribute("src", "/img/star-empty.png");
            habitsCompl[i].appendChild(imgEmpty)
        }
    }


// Change size of page
    if (!localStorage.getItem('habitsSize')) {
        localStorage.setItem('habitsSize', '20')
    }

    const sizeBtn = document.querySelector('#habits_page_size')
    sizeBtn.setAttribute('value', localStorage.getItem('habitsSize'))
    const sizeOptions = sizeBtn.nextElementSibling.children;
    for (let i = 0; i < sizeOptions.length; i++) {
        sizeOptions[i].addEventListener('click', (event) => {
            console.log(event.target.innerText)
            localStorage.setItem('habitsSize', event.target.innerText)
        })
    }

//edit habit's complexity

    const habitComplexity = document.querySelectorAll('.habit_complexity')
    habitComplexity.forEach(x => x.querySelector('.table-edit-icon').addEventListener('click', (event) => {
        event.preventDefault()
            let stars = x.querySelectorAll('img')
        function changeStar(star){
            return function () {
                let k = 0;
                for (let i = 0; i < stars.length; i++) {
                    if (star === stars[i]) {
                        k = i;
                        break;
                    }
                }
                for (let i = 0; i <= k; i++) {
                    stars[i].setAttribute('src', '/img/star-filled.png')
                }
                for (let i = k + 1; i < stars.length; i++) {
                    stars[i].setAttribute('src', '/img/star-empty.png')
                }
            }
        }
            stars.forEach(star => {
                star.style.cursor = 'pointer'
                star.addEventListener('mouseover', changeStar(star))
                star.addEventListener('mouseout', () => {
                    const complexity = x.getAttribute("data-complexity")
                    for (let i = 0; i < complexity; i++) {
                        stars[i].setAttribute('src', '/img/star-filled.png')
                    }
                    for (let i = complexity; i < stars.length; i++) {
                        stars[i].setAttribute('src', '/img/star-empty.png')
                    }
                })
                //send edit request to server
                star.addEventListener('click', () => {
                    const success = () => location.reload();
                    const hasErrors = (data) => console.log(data)
                    fetch(x.querySelector('.table-edit-icon').getAttribute('href'))
                        .then(response => {
                            let json = response.json();
                            json.then((habit) => {
                                for (let i = 0; i < stars.length; i++) {
                                    if (star === stars[i]) {
                                        habit.complexity = i + 1;
                                        break;
                                    }
                                }
                                updateHabit(habit, null, success, hasErrors)
                            })
                        })
                })

            })
          x.addEventListener('mouseleave', () => {
              stars.forEach(star => {
                  star.style.cursor = 'default'
                  const new_star = star.cloneNode(true)
                  if(star.parentElement) {
                      star.parentElement.replaceChild(new_star, star)
                  }
              })
          }, {once: true})
        })
    )

    const firstStar = document.querySelector('.complexity_1');
    const secondStar = document.querySelector('.complexity_2');
    const thirdStar = document.querySelector('.complexity_3');

    firstStar.addEventListener('click',(event) => {
        firstStar.setAttribute("src", "/img/star-filled.png");
        secondStar.setAttribute("src", "/img/star-empty.png");
        thirdStar.setAttribute("src", "/img/star-empty.png");
        document.getElementById('complexityInput').value = 1;
    })

    secondStar.addEventListener('click',(event) => {
        firstStar.setAttribute("src", "/img/star-filled.png");
        secondStar.setAttribute("src", "/img/star-filled.png");
        thirdStar.setAttribute("src", "/img/star-empty.png");
        document.getElementById('complexityInput').value = 2;
    })

    thirdStar.addEventListener('click',(event) => {
        firstStar.setAttribute("src", "/img/star-filled.png");
        secondStar.setAttribute("src", "/img/star-filled.png");
        thirdStar.setAttribute("src", "/img/star-filled.png");
        document.getElementById('complexityInput').value = 3;
    })




//set habit's complexity for filter

    const habitComplexityfilter = document.querySelectorAll('.habit_complexity_filter')
    habitComplexityfilter.forEach(x => x.querySelector('.habit_complexity_filter').addEventListener('click', (event) => {
// habitComplexityfilter.addEventListener('click', event => {
            event.preventDefault()
            let stars = habitComplexityfilter.querySelectorAll('img')
            function changeStar(star){
                return function () {
                    let k = 0;
                    for (let i = 0; i < stars.length; i++) {
                        if (star === stars[i]) {
                            k = i;
                            break;
                        }
                    }
                    for (let i = 0; i <= k; i++) {
                        stars[i].setAttribute('src', '/img/star-filled.png')
                    }
                    for (let i = k + 1; i < stars.length; i++) {
                        stars[i].setAttribute('src', '/img/star-empty.png')
                    }
                }
            }
            stars.forEach(star => {
                star.style.cursor = 'pointer'
                star.addEventListener('mouseover', changeStar(star))
                star.addEventListener('mouseout', () => {
                    const complexity = x.getAttribute("data-complexity")
                    for (let i = 0; i < complexity; i++) {
                        stars[i].setAttribute('src', '/img/star-filled.png')
                    }
                    for (let i = complexity; i < stars.length; i++) {
                        stars[i].setAttribute('src', '/img/star-empty.png')
                    }
                })
                //send edit request to server
                star.addEventListener('click', () => {
                    const success = () => location.reload();
                    const hasErrors = (data) => console.log(data)
                    fetch(x.querySelector('.table-edit-icon').getAttribute('href'))
                        .then(response => {
                            let json = response.json();
                            json.then((habit) => {
                                for (let i = 0; i < stars.length; i++) {
                                    if (star === stars[i]) {
                                        habit.complexity = i + 1;
                                        break;
                                    }
                                }
                                updateHabit(habit, null, success, hasErrors)
                            })
                        })
                })

            })
            x.addEventListener('mouseleave', () => {
                stars.forEach(star => {
                    star.style.cursor = 'default'
                    const new_star = star.cloneNode(true)
                    if(star.parentElement) {
                        star.parentElement.replaceChild(new_star, star)
                    }
                })
            }, {once: true})
        })
    )

})



// edit habit image
document.querySelectorAll('.table-download-icon').forEach(e => e.addEventListener('click', event => {
    console.log('was click')
    event.preventDefault();
    clearAllErrorsSpan();
    $('#editHabitImgModal').modal();
    fetch(event.target.getAttribute(
        'href'
    ))
        .then(response => {

            let json = response.json();
            json.then((habit) => {

                const image = document.querySelector("#upload_image");
                image.setAttribute('src', habit.image);
                const input = document.querySelector("#fileUpdate")

                $('#submitEditImgBtn').on('click', function (event) {
                    event.preventDefault();
                    clearAllErrorsSpan();

                    const hasErrors = data => {
                        data.errors.forEach(el =>
                            $(document.getElementById('errorModalUpdate' + el.fieldName)).text(el.fieldError));
                    }
                    const file    = input.files[0];
                    const success = () =>{} // location.reload()

                    updateHabit(habit, file, success, hasErrors)
                })
            })
        })
}))
//Get json from server and send back after updating object fields.
//habit - object that is needed to be send to server
//file in case upload image
//success- function that need to be invoke after successful request
//hasErrors - function that need to be invoke in case there are errors from server
const updateHabit = (habit, file = null, success, hasErrors) => {
    let result = new FormData();
    result.append("habitManagementDto", new Blob([JSON.stringify(habit)], {type: "application/json"}));
    if (file) {
        result.append("file", file)
    }
    $.ajax({
        url: '/management/habits/update',
        type: 'put',
        dataType: 'json',
        contentType: false,
        processData: false,
        cache: false,
        success: (data) => {
            if (Array.isArray(data.errors) && data.errors.length) {
                hasErrors(data)
            } else {
                success()
            }
        },
        data: result
    });
}

const loadFile = function (event) {
    const image = document.querySelector("#upload_image");
    const file    = document.querySelector('#fileUpdate').files[0];
    let reader = new FileReader();
    reader.onloadend = () => {
        image.src = reader.result
        console.log(image.src)
    };
    if (file) {
        reader.readAsDataURL(file);
    }
};
