import { apiRequest } from './apiClient.js'; // 공통 모듈 불러오기

// 사용자 데이터를 가져와서 HTML 업데이트
async function fetchUserData() {
    try {
        const data = await apiRequest('/api/user/mypage', {
            method: 'GET',
        });

        // 사용자 정보 업데이트
        document.getElementById('welcome-message').textContent = `Welcome, ${data.userName}`;
        document.getElementById('nickname').textContent = data.nickname;
        document.getElementById('email').textContent = data.email;
    } catch (error) {
        if(error.message==='Require Login'){
            alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
            window.location.href = '/html/login.html';
        }else {
            console.error('Error fetching user data:', error);
            alert('사용자 정보를 불러오지 못했습니다. 다시 시도해주세요.');
        }
    }
}

// 내가 쓴 리뷰 가져오기
async function fetchWrittenReviews(page = 0, size = 5) {
    try {
        const data = await apiRequest(`/api/reviews/reviewer/mypage/paged?page=${page}&size=${size}`, { method: 'GET' });

        const reviewsContainer = document.getElementById('written-reviews-container');
        reviewsContainer.innerHTML = ''; // 기존 내용을 비움

        if (data.content.length === 0) {
            reviewsContainer.innerHTML = '<p>내가 쓴 리뷰가 없습니다.</p>';
            return;
        }

        data.content.forEach((review) => {
            const reviewElement = document.createElement('div');
            reviewElement.classList.add('review-item');
            reviewElement.innerHTML = `
                <p><strong>평점:</strong> ${review.rating}</p>
                <p><strong>내용:</strong> ${review.content}</p>
                <p><strong>작성일:</strong> ${new Date(review.createdAt).toLocaleDateString()}</p>
            `;
            reviewsContainer.appendChild(reviewElement);
        });

        renderPagination(data.totalPages, page, fetchWrittenReviews, 'written-pagination-container');
    } catch (error) {
        console.error('Error fetching written reviews:', error);
    }
}

// 내가 받은 리뷰 가져오기
async function fetchReceivedReviews(page = 0, size = 5) {
    try {
        const data = await apiRequest(`/api/reviews/reviewee/mypage/paged?page=${page}&size=${size}`, { method: 'GET' });

        const reviewsContainer = document.getElementById('received-reviews-container');
        reviewsContainer.innerHTML = ''; // 기존 내용을 비움

        if (data.content.length === 0) {
            reviewsContainer.innerHTML = '<p>내가 받은 리뷰가 없습니다.</p>';
            return;
        }

        data.content.forEach((review) => {
            const reviewElement = document.createElement('div');
            reviewElement.classList.add('review-item');
            reviewElement.innerHTML = `
                <p><strong>평점:</strong> ${review.rating}</p>
                <p><strong>내용:</strong> ${review.content}</p>
                <p><strong>작성일:</strong> ${new Date(review.createdAt).toLocaleDateString()}</p>
            `;
            reviewsContainer.appendChild(reviewElement);
        });

        renderPagination(data.totalPages, page, fetchReceivedReviews, 'received-pagination-container');
    } catch (error) {
        console.error('Error fetching received reviews:', error);
    }
}

// 페이지네이션 버튼 렌더링
function renderPagination(totalPages, currentPage, fetchFunction, containerId) {
    const paginationContainer = document.getElementById(containerId);
    paginationContainer.innerHTML = ''; // 기존 내용을 비움

    for (let i = 0; i < totalPages; i++) {
        const pageButton = document.createElement('button');
        pageButton.textContent = i + 1;
        pageButton.classList.add('page-btn');
        if (i === currentPage) pageButton.classList.add('active');
        pageButton.addEventListener('click', () => fetchFunction(i)); // 페이지 이동 이벤트
        paginationContainer.appendChild(pageButton);
    }
}


// 탭 전환
function showTab(tabId) {
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => tab.classList.remove('active'));

    const contents = document.querySelectorAll('.tab-content');
    contents.forEach(content => content.classList.remove('active'));

    document.getElementById(tabId).classList.add('active');
    event.target.classList.add('active');

    // 내가 쓴 리뷰 탭 클릭 시 데이터 로드
    if (tabId === 'written-reviews') {
        fetchWrittenReviews();
    }
    // 내가 받은 리뷰 탭 클릭 시 데이터 로드 ✅ 수정된 부분
    else if (tabId === 'received-reviews') {
        fetchReceivedReviews();
    }
}

// 프로필 수정 페이지로 이동
function openEditPage() {
    window.location.href = "edit-profile.html";
}

// 이벤트 리스너 등록
function initializeEventListeners() {
    // 프로필 수정 버튼 클릭 이벤트
    document.getElementById('edit-profile-btn').addEventListener('click', openEditPage);

    // 탭 버튼 클릭 이벤트
    document.querySelectorAll('.tab-btn').forEach(button => {
        button.addEventListener('click', (event) => {
            const tabId = event.target.getAttribute('data-tab');
            showTab(tabId);
        });
    });
}

// DOMContentLoaded 이벤트에서 초기화 실행
document.addEventListener('DOMContentLoaded', () => {
    fetchUserData(); // 사용자 데이터 가져오기
    fetchWrittenReviews(); // 내가 쓴 리뷰 가져오기
    initializeEventListeners(); // 이벤트 리스너 등록
});

