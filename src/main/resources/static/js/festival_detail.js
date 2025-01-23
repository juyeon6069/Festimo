window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('festival_id');

    if (eventId) {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    const event = JSON.parse(xhr.responseText.trim());
                    displayEventDetails(event);
                } else {
                    alert(`[에러] 축제 상세 정보 요청 실패: ${xhr.status}`);
                }
            }
        }
        xhr.open("GET", `/api/events/${eventId}`, true);
        xhr.send();
    } else {
        alert("축제가 존재하지 않습니다.");
    }
}
function displayEventDetails(event) {
    document.title = `${event.title} - Festimo`;

    document.querySelector('.event-title').innerHTML = `${event.title} <span class="event-category">${event.category}</span>`;

    // 진행상태 뱃지
    const badge = document.querySelector('.event-badge');
    const today = new Date();

    const startDate = new Date(event.startDate);
    const endDate = new Date(event.endDate);

    let badgeText = '';
    if (today >= startDate && today <= endDate) {
        badgeText = '진행중';
    } else if (today < startDate) {
        badgeText = '예정';
    } else if (today > endDate) {
        badgeText = '종료';
    }

    badge.textContent = badgeText;


    const imageElement = document.querySelector('.event-image img');
    if(!event.image){
        imageElement.src = "/imgs/alt_img.jpg"
    } else {
        imageElement.src = event.image;
    }
    imageElement.alt = event.title;

    document.querySelectorAll('.info-row .info-value')[0].innerHTML = `${event.startDate} ~ ${event.endDate}`;

    document.querySelectorAll('.info-row .info-value')[1].innerHTML = event.address;

    document.querySelectorAll('.info-row .info-value')[2].innerHTML = event.phone;

    let mainDescription = "";
    let programDetails = "";

    event.festivalDetails.details.forEach((detail) => {
        if (detail.infoName === "행사소개") {
            mainDescription = detail.infoText;
        } else if (detail.infoName === "행사내용") {
            programDetails = detail.infoText;
        }
    });

    const descriptions = document.querySelectorAll(".event-description");
    if (descriptions.length > 0) {
        descriptions[0].textContent = mainDescription; // 메인 설명
    }
    if (descriptions.length > 1) {
        descriptions[1].innerHTML = programDetails;
    }

    loadMap(event.ycoordinate, event.xcoordinate);

}

function loadMap(latitude, longitude) {
    fetch('/api/map-key')
        .then(response => response.text())
        .then(apiKey => {
            const script = document.getElementById('kakao-map-script');
            if (script) {
                script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false`;
                script.onload = () => {
                    kakao.maps.load(() => {
                        initMap(latitude, longitude);
                    });
                };
            } else {
                console.error('카카오 지도 스크립트 태그를 찾을 수 없습니다.');
            }
        })
        .catch(error => console.error('Error fetching API key:', error));
}

// 지도 초기화 함수
function initMap(latitude, longitude) {
    const container = document.getElementById('map');

    const options = {
        center: new kakao.maps.LatLng(latitude, longitude),
        level: 3,
    };

    // 지도 생성
    const map = new kakao.maps.Map(container, options);

    // 마커 추가
    const markerPosition = new kakao.maps.LatLng(latitude, longitude); // 마커 위치
    const marker = new kakao.maps.Marker({
        position: markerPosition,
    });
    marker.setMap(map);

}

document.querySelector('.back-button').addEventListener('click', function (event) {
    event.preventDefault();
    window.history.back();
});