"use strict";
const confirmationModal = new ConfirmationModal('confirmationModal', 'confirmationMessage', 'confirmModalButton', 'cancelModalButton');
const toggleAllCheckBox = document.getElementById("selectAll");
const checkboxes = document.querySelector("tbody").querySelectorAll("input[type='checkbox']");
const deleteButton = document.getElementById("deleteSelectedButton")


deleteButton.addEventListener('click', () => {
    confirmationModal.show(
        'Are you sure you want to delete selected?',
        () => {
            const ids = Array.from(checkboxes)
                .filter(checkbox => checkbox.checked)
                .map(checkbox => checkbox.value);

            fetch(`${backendAddress}/management/events`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(ids)
            }).then(r => {
                if (r.ok) {
                    window.location.reload();
                }
            })
        },
        () => {
        }
    );
});


function toggleAllCheckboxes() {
    const isChecked = toggleAllCheckBox.checked;
    checkboxes.forEach(checkbox => {
        checkbox.checked = isChecked;
    });
    updateToggleAllCheckbox()
}

function updateToggleAllCheckbox() {
    const checkedCheckboxes = Array.from(checkboxes).filter(checkbox => checkbox.checked);
    toggleAllCheckBox.checked = checkedCheckboxes.length === checkboxes.length;
    toggleAllCheckBox.indeterminate = checkedCheckboxes.length > 0 && checkedCheckboxes.length < checkboxes.length;
    toggleIndeterminate(toggleAllCheckBox.indeterminate);
    toggleDeleteButton(checkedCheckboxes.length > 0)
}

toggleAllCheckBox.addEventListener('change', toggleAllCheckboxes);

document.querySelector("tbody").addEventListener('change', event => {
    if (event.target.type === 'checkbox') {
        updateToggleAllCheckbox();
    }
});

function toggleIndeterminate(isIndeterminate) {
    if (isIndeterminate) {
        toggleAllCheckBox.classList.add('indeterminate')
    } else {
        toggleAllCheckBox.classList.remove('indeterminate')
    }
}

function toggleDeleteButton(isAnySelected) {
    if (isAnySelected) {
        deleteButton.classList.remove("d-none")
    } else {
        deleteButton.classList.add("d-none")
    }
}

