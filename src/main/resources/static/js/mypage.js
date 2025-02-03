import { apiRequest } from './apiClient.js'; // 공통 모듈 불러오기

// 사용자 데이터를 가져와서 HTML 업데이트
async function fetchUserData() {
    try {
        const data = await apiRequest('/api/user/mypage', {
            method: 'GET',
        });

        // 사용자 정보 업데이트
        document.getElementById('welcome-message').textContent = `${data.userName}`;
        document.getElementById('nickname').textContent = data.nickname;
        document.getElementById('email').textContent = data.email;

        fetchFollowCounts(data.id);
        window.currentUserId = data.id;
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

// 추가: 특정 사용자 ID에 대해 팔로워/팔로잉 수를 별도로 가져오는 함수
async function fetchFollowCounts(userId) {
    try {
        // 팔로워 수 가져오기
        const followersCount = await apiRequest(`/api/follow/followers/count?userId=${userId}`, { method: 'GET' });
        // 팔로잉 수 가져오기
        const followingCount = await apiRequest(`/api/follow/following/count?userId=${userId}`, { method: 'GET' });

        document.getElementById('followers-count').textContent = followersCount;
        document.getElementById('following-count').textContent = followingCount;
    } catch (error) {
        console.error('Error fetching follow counts:', error);
    }
}

// 팔로워 목록을 가져와 모달에 표시하는 함수
async function fetchFollowersList(userId) {
    try {
        const data = await apiRequest(`/api/follow/followers/${userId}`, { method: 'GET' });
        showFollowModal("팔로워 목록", data);
    } catch (error) {
        console.error('Error fetching followers list:', error);
        alert('팔로워 목록을 불러오지 못했습니다.');
    }
}

// 팔로잉 목록을 가져와 모달에 표시하는 함수
async function fetchFollowingList(userId) {
    try {
        const data = await apiRequest(`/api/follow/following/${userId}`, { method: 'GET' });
        showFollowModal("팔로잉 목록", data);
    } catch (error) {
        console.error('Error fetching following list:', error);
        alert('팔로잉 목록을 불러오지 못했습니다.');
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
                <div class="review-header">
                    <span class="review-rating">${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</span>
                    <span class="review-date">${new Date(review.createdAt).toLocaleDateString()}</span>
                </div>
                <div class="review-content">
                    <p>${review.content}</p>
                </div>
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
                <div class="review-header">
                    <span class="review-rating">${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</span>
                    <span class="review-date">${new Date(review.createdAt).toLocaleDateString()}</span>
                </div>
                <div class="review-content">
                    <p>${review.content}</p>
                </div>
            `;
            reviewsContainer.appendChild(reviewElement);
        });

        renderPagination(data.totalPages, page, fetchReceivedReviews, 'received-pagination-container');
    } catch (error) {
        console.error('Error fetching received reviews:', error);
    }
}

// 모달 창을 열고 목록을 표시하는 함수
function showFollowModal(title, listData) {
    const modal = document.getElementById('follow-modal');
    const modalTitle = document.getElementById('modal-title');
    const followList = document.getElementById('follow-list');

    modalTitle.textContent = title;
    followList.innerHTML = ''; // 기존 목록 초기화

    // listData가 배열인 경우
    if (Array.isArray(listData) && listData.length > 0) {
        listData.forEach(item => {
            const li = document.createElement('li');
            const nicknameSpan = document.createElement('span');
            nicknameSpan.textContent = item.nickname;
            nicknameSpan.classList.add('follow-item-nickname');
            li.appendChild(nicknameSpan);

            followList.appendChild(li);
        });
    } else {
        followList.innerHTML = '<li>표시할 사용자가 없습니다.</li>';
    }
    modal.style.display = 'block';  // 모달 보이기
}

// 모달 닫기 함수
function closeFollowModal() {
    document.getElementById('follow-modal').style.display = 'none';
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

    // 팔로잉/팔로워 클릭 이벤트 등록
    document.getElementById('following-count').parentElement.addEventListener('click', () => {
        fetchFollowingList(window.currentUserId);
    });
    document.getElementById('followers-count').parentElement.addEventListener('click', () => {
        fetchFollowersList(window.currentUserId);
    });

    // 모달 닫기 버튼 이벤트 등록
    document.getElementById('modal-close').addEventListener('click', closeFollowModal);

    // 모달 외부 클릭 시 닫기 (옵션)
    window.addEventListener('click', (event) => {
        const modal = document.getElementById('follow-modal');
        if (event.target === modal) {
            closeFollowModal();
        }
    });
}

// DOMContentLoaded 이벤트에서 초기화 실행
document.addEventListener('DOMContentLoaded', () => {
    fetchUserData(); // 사용자 데이터 가져오기
    fetchFollowCounts(); // 팔로우.팔로잉 수 가져오기
    fetchWrittenReviews(); // 내가 쓴 리뷰 가져오기
    initializeEventListeners(); // 이벤트 리스너 등록
});
