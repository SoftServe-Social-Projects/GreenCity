function initializeCarousel(carouselId, imageUrls) {
    let numImages = imageUrls ? imageUrls.length : 0;
    let indicatorsHtml = "";
    let innerHtml = "";
    if (numImages > 0) {
        for (let i = 0; i < numImages; i++) {
            indicatorsHtml += `
                         <li data-target="#${carouselId}" data-slide-to="${i}"${i === 0 ? ' class="active"' : ''}></li>
                        `;
            innerHtml += `
                        <div class="carousel-item${i === 0 ? ' active' : ''}">
                            <img src="${imageUrls[i]}" class="d-block w-100" alt="Image ${i + 1}">
                        </div>
                    `;
        }
        $('#' + carouselId + ' .carousel-indicators').html(indicatorsHtml);
        $('#' + carouselId + ' .carousel-inner').html(innerHtml);
    } else {
        $('#' + carouselId).hide();
    }
}