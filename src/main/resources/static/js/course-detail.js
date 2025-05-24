document.addEventListener('DOMContentLoaded', () => {
    const cards = document.querySelectorAll('.place-card');

    cards.forEach(card => {
        const placeId = card.dataset.placeId;
        const container = card.querySelector('.js-photo-container');

        fetch(`/api/place/photos?placeId=${placeId}`)
            .then(res => res.json())
            .then(photoUrls => {
                if (!photoUrls.length) {
                    container.innerText = '이미지 없음';
                    return;
                }

                // 캐러셀 요소 생성
                const wrapper = document.createElement('div');
                wrapper.className = 'carousel-container';

                const track = document.createElement('div');
                track.className = 'carousel-track';

                photoUrls.forEach(url => {
                    const img = document.createElement('img');
                    img.src = url;
                    img.alt = '장소 이미지';
                    img.className = 'carousel-image';

                    track.appendChild(img);
                });

                const prevBtn = document.createElement('div');
                prevBtn.className = 'carousel-nav carousel-prev';
                prevBtn.innerHTML = '<i class="fas fa-chevron-left"></i>';

                const nextBtn = document.createElement('div');
                nextBtn.className = 'carousel-nav carousel-next';
                nextBtn.innerHTML = '<i class="fas fa-chevron-right"></i>';

                // 슬라이드 이동 처리
                let currentIndex = 0;
                const slideWidth = 265;

                prevBtn.addEventListener('click', () => {
                    if (currentIndex > 0) {
                        currentIndex--;
                        track.scrollTo({
                            left: currentIndex * slideWidth,
                            behavior: 'smooth'
                        });
                    }
                });

                nextBtn.addEventListener('click', () => {
                    if (currentIndex < photoUrls.length - 1) {
                        currentIndex++;
                        track.scrollTo({
                            left: currentIndex * slideWidth,
                            behavior: 'smooth'
                        });
                    }
                });

                wrapper.appendChild(prevBtn);
                wrapper.appendChild(track);
                wrapper.appendChild(nextBtn);
                container.appendChild(wrapper);
            })
            .catch(err => {
                console.error(err);
                container.innerText = 'API 오류';
            });

        // 리뷰 표시 로직
        const reviewsContainer = card.querySelector('.place-reviews');

        const service = new google.maps.places.PlacesService(document.createElement('div'));

        service.getDetails({
            placeId: placeId,
            fields: ['reviews']
        }, (place, status) => {
            if (status === google.maps.places.PlacesServiceStatus.OK && place.reviews) {
                let html = '<h4>리뷰</h4>';
                place.reviews.slice(0, 3).forEach(review => {
                    html += `
                <div class="review-item">
                    <div class="review-header">
                        <span class="review-author">${review.author_name}</span>
                        <span class="review-rating">${review.rating}★</span>
                    </div>
                    <div class="review-text">${review.text}</div>
                </div>
            `;
                });
                reviewsContainer.innerHTML = html;
            } else {
                reviewsContainer.innerHTML = `
            <div class="review-item">
                <div class="review-text">리뷰가 없습니다.</div>
            </div>
        `;
            }
        });

    });
});