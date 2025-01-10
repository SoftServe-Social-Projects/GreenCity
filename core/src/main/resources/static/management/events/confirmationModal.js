class ConfirmationModal {
    constructor(modalId, messageElementId, confirmButtonId, cancelButtonId) {
        this.modalId = modalId;
        this.messageElementId = messageElementId;
        this.confirmButtonId = confirmButtonId;
        this.cancelButtonId = cancelButtonId;
        this.callbackConfirm = null;
        this.callbackCancel = null;

        this.setupEventListeners();
    }

    setupEventListeners() {
        document.getElementById(this.confirmButtonId).addEventListener('click', () => {
            if (this.callbackConfirm) this.callbackConfirm();
            this.hide();
        });

        $(`#${this.modalId}`).on('hide.bs.modal', () => {
            if (this.callbackCancel) this.callbackCancel();
        });
    }

    show(message, onConfirm, onCancel, confirmButtonText = "Yes", cancelButtonText = "No") {
        document.getElementById(this.messageElementId).textContent = message;
        document.getElementById(this.confirmButtonId).textContent = confirmButtonText;
        document.getElementById(this.cancelButtonId).textContent = cancelButtonText;

        this.callbackConfirm = onConfirm;
        this.callbackCancel = onCancel;

        $(`#${this.modalId}`).modal('show');
    }

    hide() {
        $(`#${this.modalId}`).modal('hide');
    }
}