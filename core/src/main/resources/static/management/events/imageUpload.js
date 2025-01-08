class ImageUploader {
    constructor({ fileInputSelector, previewContainerSelector, proposedImagesContainerSelector, errorMessageSelector, maxFiles = 5 }) {
        this.currentFiles = [];
        this.fileInput = document.querySelector(fileInputSelector);
        this.previewContainer = document.querySelector(previewContainerSelector);
        this.proposedImagesContainer = document.querySelector(proposedImagesContainerSelector);
        this.errorMessage = document.querySelector(errorMessageSelector);
        this.maxFiles = maxFiles;

        if (this.fileInput) {
            this.fileInput.addEventListener('change', this.previewImages.bind(this));
        }
    }

    loadProposedImages(imageSrcArray) {
        this.proposedImagesContainer.innerHTML = '';

        imageSrcArray.forEach(src => {
            const container = document.createElement('div');
            container.className = 'img-container';

            const img = document.createElement('img');
            img.src = src;
            img.className = 'proposed-img';
            img.dataset.fileName = src.split('/').pop();
            img.onclick = () => this.selectProposedImage(img);

            container.appendChild(img);
            this.proposedImagesContainer.appendChild(container);
        });
    }

    previewImages(event) {
        const files = event.target.files;
        const newFiles = Array.from(files).filter(file => this.validateFile(file));

        if (newFiles.length + this.currentFiles.length > this.maxFiles) {
            this.showError('Please upload 5 pictures max');
            return;
        }

        newFiles.forEach(file => {
            const reader = new FileReader();
            reader.onload = (e) => {
                this.addImageToPreview(e.target.result, file.name, true);
                this.currentFiles.push(file);
                this.updateFileList();
            };
            reader.readAsDataURL(file);
        });
    }

    validateFile(file) {
        if (!['image/jpeg', 'image/png'].includes(file.type)) {
            this.showError('Please upload pictures in PNG or JPG format only');
            return false;
        }
        if (file.size > 10 * 1024 * 1024) {
            this.showError('Please upload pictures less than 10MB');
            return false;
        }
        if (this.currentFiles.some(f => f.name === file.name)) {
            this.showError('Duplicate file detected: ' + file.name);
            return false;
        }
        return true;
    }

    selectProposedImage(imgElement) {
        const fileName = imgElement.dataset.fileName;
        const imgSrc = imgElement.src;

        if (this.currentFiles.some(f => f.name === fileName)) {
            this.showError('Duplicate file detected: ' + fileName);
            return;
        }

        if (this.currentFiles.length >= this.maxFiles) {
            this.showError('Please upload 5 pictures max');
            return;
        }

        fetch(imgSrc)
            .then(response => response.blob())
            .then(blob => {
                const file = new File([blob], fileName, { type: blob.type });

                this.addImageToPreview(imgSrc, fileName, false);

                this.currentFiles.push(file);
                this.updateFileList();
            })
            .catch(error => {
                this.showError('Error selecting proposed image: ' + error.message);
            });
    }

    addImageToPreview(imgSrc, fileName, isUploaded) {
        if (!this.previewContainer) {
            console.error('Preview container element not found');
            return;
        }

        const container = document.createElement('div');
        container.className = 'img-container';

        const img = document.createElement('img');
        img.src = imgSrc;
        img.dataset.fileName = fileName;

        const deleteButton = document.createElement('button');
        deleteButton.textContent = '×';
        deleteButton.className = 'delete-button';
        deleteButton.onclick = () => {
            container.remove();
            this.currentFiles = this.currentFiles.filter(f => f.name !== fileName);
            this.updateFileList();
        };

        container.appendChild(img);
        container.appendChild(deleteButton);

        this.previewContainer.appendChild(container);
    }

    updateFileList() {
        const dataTransfer = new DataTransfer();

        this.currentFiles.forEach(file => {
            dataTransfer.items.add(file);
        });

        this.fileInput.files = dataTransfer.files;
    }

    showError(message) {
        if (this.errorMessage) {
            this.errorMessage.textContent = message;
            setTimeout(() => {
                this.errorMessage.textContent = '';
            }, 3000);
        }
    }
}

