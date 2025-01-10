$(document).on('click', '.edit', function (event) {
    event.preventDefault();

    const row = $(this).closest('tr');
    const id = row.find('td').eq(0).text();
    const name = row.find('td').eq(1).text();
    const points = row.find('td').eq(2).text();
    const status = row.find('td').eq(3).text();

    $('#editId').val(id);
    $('#editName').val(name);
    $('#editPoints').val(points);
    $('#editStatus').val(status);

    $('#editModal').modal('show');
});

// Handle the edit form submission
$(document).on('click','#editModalSubmit', function (event) {
    event.preventDefault();

    const id = $('#editId').val();
    const name = $('#editName').val();
    const points = $('#editPoints').val();
    const status = $('#editStatus').val();

    $.ajax({
        url: '/management/rating/calculation',
        type: 'PUT',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({
            id: id,
            name: name,
            points: points,
            status: status
        }),
        success: function (response) {
            location.reload();
        },
        error: function (error) {
            alert('Error updating rating points: ' + error.responseText);
        }
    });
});


// Function to handle the delete button click
$(document).on('click', '.delete', function (event) {
    event.preventDefault();

    const row = $(this).closest('tr');
    const id = row.find('td').eq(0).text();

    $.ajax({
        url: `/management/rating/calculation/${id}`,
        type: 'DELETE',
        success: function(result) {
            location.reload();
        },
        error: function(xhr, status, error) {
            alert("Error deleting rating points: " + xhr.responseText);
        }
    });
});

$(document).on('click', '.restore', function (event) {
    event.preventDefault();

    const row = $(this).closest('tr');
    const id = row.find('td').eq(0).text();

    $.ajax({
        url: `/management/rating/calculation/restore/${id}`,
        type: 'PUT',
        success: function(result) {
            location.reload();
        },
        error: function(xhr, status, error) {
            alert("Error deleting rating points: " + xhr.responseText);
        }
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
        'name': ['name-icon-asc', 'name-icon-desc'],
        'points': ['points-icon-asc', 'points-icon-desc']
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
