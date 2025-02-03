import { apiRequest, getCurrentUserId, isLoggedIn } from './apiClient.js';

// 전역 함수로 등록
window.loadApplications = loadApplications;
window.handleWithdraw = handleWithdraw;
window.acceptApplication = acceptApplication;
window.rejectApplication = rejectApplication;

window.completeCompanion = completeCompanion;
window.restoreCompanion = restoreCompanion;

window.openEditTitleModal = openEditTitleModal;
window.closeEditTitleModal = closeEditTitleModal;
window.updateCompanionTitle = updateCompanionTitle;

window.toggleMemberView = function(view) {
    document.getElementById('ongoing-members').classList.toggle('hidden', view !== 'ongoing');
    document.getElementById('completed-members').classList.toggle('hidden', view !== 'completed');
};

window.toggleLeaderView = function(view) {
    document.getElementById('ongoing-leaders').classList.toggle('hidden', view !== 'ongoing');
    document.getElementById('completed-leaders').classList.toggle('hidden', view !== 'completed');
};



document.addEventListener('DOMContentLoaded', () => {
    console.log("isLoggedIn: ", isLoggedIn()); // 현재 로그인 상태 확인

    // 페이지 로드 시 로그인 여부 확인
    if (!isLoggedIn()) {
        alert('로그인이 필요합니다.');
        console.log("Redirecting to login page...");
        window.location.href = '/html/login.html'; // 로그인 페이지로 이동
        return;
    }



    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

            tab.classList.add('active');
            const tabId = tab.dataset.tab;
            document.getElementById(`${tabId}Tab`).classList.add('active');
        });
    });
    fetchCompanions();
});

function renderCompanionItem(name, isLeader, userId, currentUserId) {
    console.log("renderCompanionItem : ", { name, isLeader, userId, currentUserId });

    const showReviewButton = userId !== currentUserId; // 본인이면 버튼 숨기기

    return `
        <div class="companion-item">
            <div class="user-icon">
                <span class="material-icons">person</span>
            </div>
            <div class="companion-info">
                <span class="companion-name">${name}</span>
                ${isLeader ? '<span class="leader-badge">리더</span>' : ''}
            </div>
            ${showReviewButton ? `
                <button class="btn write-review-btn" data-id="${userId}" data-name="${name}">
                    리뷰 작성
                </button>
            ` : ''}
        </div>
    `;
}


async function fetchCompanions() {
    try {
        document.getElementById('leaderContent').innerHTML = '<div class="loading">로딩 중...</div>';
        document.getElementById('memberContent').innerHTML = '<div class="loading">로딩 중...</div>';

        const [data, currentUserId] = await Promise.all([
            apiRequest('/api/meet/companions/mine'),
            getCurrentUserId()
        ]);

        console.log("Fetched Data:", data);
        console.log("Current User ID:", currentUserId);

        renderCompanions(data, currentUserId);
        return data;
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('leaderContent').innerHTML = '<div class="error">데이터를 불러오는데 실패했습니다.</div>';
        document.getElementById('memberContent').innerHTML = '<div class="error">데이터를 불러오는데 실패했습니다.</div>';
        return { asLeader: [], asMember: [] };
    }
}



function renderCompanions(data, currentUserId) {
    console.log("Leaders Data:", data.asLeader);
    console.log("Members Data:", data.asMember);

    //  리더로 참여한 동행을 진행 중(ONGOING)과 완료됨(COMPLETED)으로 분리
    const ongoingLeaders = data.asLeader.filter(companion => companion.status === "ONGOING");
    const completedLeaders = data.asLeader.filter(companion => companion.status === "COMPLETED");

    //동행원으로 참여한 동행을 진행 중(ONGOING)과 완료됨(COMPLETED)으로 분리
    const ongoingMembers = data.asMember.filter(companion => companion.status === "ONGOING");
    const completedMembers = data.asMember.filter(companion => companion.status === "COMPLETED");

    //리더로 참여한 동행 렌더링
    const leaderContent = document.getElementById('leaderContent');
    leaderContent.innerHTML = `
        <div class="toggle-buttons">
            <button class="btn toggle-btn" onclick="toggleLeaderView('ongoing')">진행중 </button>
            <button class="btn toggle-btn" onclick="toggleLeaderView('completed')">완료</button>
        </div>
        <div id="ongoing-leaders" class="companion-group">
            ${ongoingLeaders.length > 0 ? ongoingLeaders.map(companion => renderCompanionCard(companion, currentUserId)).join('')
        : '<p class="empty-message">진행 중인 동행이 없습니다.</p>'}
        </div>
        <div id="completed-leaders" class="companion-group hidden">
            ${completedLeaders.length > 0 ? completedLeaders.map(companion => renderCompanionCard(companion, currentUserId)).join('')
        : '<p class="empty-message">완료된 동행이 없습니다.</p>'}
        </div>
    `;

    // 멤버로 참여한 동행 렌더링
    const memberContent = document.getElementById('memberContent');
    memberContent.innerHTML = `
        <div class="toggle-buttons">
            <button class="btn toggle-btn" onclick="toggleMemberView('ongoing')">진행 중 보기</button>
            <button class="btn toggle-btn" onclick="toggleMemberView('completed')">완료된 보기</button>
        </div>
        <div id="ongoing-members" class="companion-group">
            ${ongoingMembers.length > 0 ? ongoingMembers.map(companion => renderCompanionCard(companion, currentUserId)).join('')
        : '<p class="empty-message">진행 중인 동행이 없습니다.</p>'}
        </div>
        <div id="completed-members" class="companion-group hidden">
            ${completedMembers.length > 0 ? completedMembers.map(companion => renderCompanionCard(companion, currentUserId)).join('')
        : '<p class="empty-message">완료된 동행이 없습니다.</p>'}
        </div>
    `;
}

function toggleLeaderView(view) {
    document.getElementById('ongoing-leaders').classList.toggle('hidden', view !== 'ongoing');
    document.getElementById('completed-leaders').classList.toggle('hidden', view !== 'completed');
}

function toggleMemberView(view) {
    document.getElementById('ongoing-members').classList.toggle('hidden', view !== 'ongoing');
    document.getElementById('completed-members').classList.toggle('hidden', view !== 'completed');
}
function renderCompanionCard(companion, currentUserId) {
    const isLeader = companion.leaderId === currentUserId;
    const isCompleted = companion.status === 'COMPLETED';

    // title 이스케이프 처리를 좀 더 강화
    const escapedTitle = companion.title
        .replace(/'/g, '&#39;')
        .replace(/"/g, '&quot;')
        .replace(/`/g, '&#96;');

    return `
        <div class="companion-card ${isCompleted ? 'completed' : ''}">
            <h2 class="companion-title">${companion.title}</h2>
            <div class="card-actions">
                ${isLeader
        ? `<button class="btn edit-title-btn" onclick="openEditTitleModal(${companion.companionId}, '${escapedTitle}')">이름 수정</button>
                       ${isCompleted
            ? `<button class="btn restore-btn" onclick="restoreCompanion(${companion.companionId})">진행하기</button>`
            : `<button class="btn complete-btn" onclick="completeCompanion(${companion.companionId})">종료하기</button>`
        }
                       <button class="btn" onclick="loadApplications(${companion.companionId})">신청 리스트 확인</button>`
        : ''
    }
            </div>
            <div class="companions-list">
                ${companion.members.map(member =>
        renderCompanionItem(member.userName, member.userId === companion.leaderId, member.userId, currentUserId)
    ).join('')}
            </div>
        </div>
    `;
}


async function completeCompanion(companionId) {
    if (!confirm('정말로 이 동행을 종료하시겠습니까?')) return;

    try {
        await apiRequest(`/api/meet/${companionId}/status/completed`, {
            method: "PATCH"
        });
        alert("동행이 종료되었습니다.");
        fetchCompanions(); // 동행 목록 새로고침
    } catch (error) {
        console.error("Error completing companion:", error);
        alert("동행 종료 처리 중 오류가 발생했습니다.");
    }
}

async function restoreCompanion(companionId) {
    if (!confirm('이 동행을 다시 진행 상태로 변경하시겠습니까?')) return;

    try {
        await apiRequest(`/api/meet/${companionId}/status/ongoing`, {
            method: "PATCH"
        });
        alert("동행이 진행 상태로 변경되었습니다.");
        fetchCompanions(); // 동행 목록 새로고침
    } catch (error) {
        console.error("Error restoring companion:", error);
        alert("동행 진행 복원 중 오류가 발생했습니다.");
    }
}

let editingCompanionId = null; // 수정 중인 동행 ID 저장

function openEditTitleModal(companionId, currentTitle) {
    editingCompanionId = companionId; // 현재 수정할 동행 ID 저장
    document.getElementById('editTitleInput').value = currentTitle; // 현재 이름 채우기
    document.getElementById('editTitleModal').style.display = 'block'; // 모달 열기
}

function closeEditTitleModal() {
    document.getElementById('editTitleModal').style.display = 'none'; // 모달 닫기
}

async function updateCompanionTitle() {
    if (!editingCompanionId) return;

    const newTitle = document.getElementById('editTitleInput').value.trim();
    if (!newTitle) {
        alert('동행 이름을 입력하세요.');
        return;
    }

    try {
        await apiRequest(`/api/meet/${editingCompanionId}/title`, {
            method: "PATCH",
            body: JSON.stringify(newTitle), // 서버에 새로운 이름 전송
            headers: {
                "Content-Type": "application/json"
            }
        });
        alert('동행 이름이 변경되었습니다.');
        closeEditTitleModal();
        fetchCompanions(); // 변경된 내용 반영을 위해 다시 불러오기
    } catch (error) {
        console.error("Error updating companion title:", error);
        alert("동행 이름 변경 중 오류가 발생했습니다.");
    }
}



async function loadApplications(companionId) {
    const modal = document.getElementById('applicationModal');
    modal.classList.add('active');

    try {
        const data = await apiRequest(`/api/meet/companion/${companionId}`);
        const applicationTable = document.getElementById("application-table");
        applicationTable.innerHTML = "";

        if (!data || !Array.isArray(data) || data.length === 0) {
            applicationTable.innerHTML = '<div class="application-row">신청된 내역이 없습니다.</div>';
            return;
        }

        data.forEach(application => {
            const row = document.createElement("div");
            row.classList.add("application-row");

            row.innerHTML = `
                <div class="application-info">
                    <div>
                        <span>닉네임</span>
                        <div class="font-medium mt-1">${application.nickname}</div>
                    </div>
                    <div>
                        <span>성별</span>
                        <div class="font-medium mt-1">${application.gender === 'MALE' ? '남성' : '여성'}</div>
                    </div>
                    <div>
                        <span>평점</span>
                        <div class="font-medium mt-1">${application.ratingAvg.toFixed(1)}</div>
                    </div>
                </div>
                <div class="application-actions">
                    <button onclick="acceptApplication(${application.applicationId}, ${companionId})" class="accept-button">
                        <span class="material-icons">check</span>
                        수락
                    </button>
                    <button onclick="rejectApplication(${application.applicationId}, ${companionId})" class="reject-button">
                        <span class="material-icons">close</span>
                        거절
                    </button>
                </div>
            `;
            applicationTable.appendChild(row);
        });
    } catch (error) {
        console.error("Error loading applications:", error);
        document.getElementById("application-table").innerHTML =
            '<div class="application-row">데이터를 불러오는데 실패했습니다.</div>';
    }
}



// 모달 닫기 버튼 이벤트
document.querySelector('.close-btn').addEventListener('click', () => {
    const modal = document.getElementById('applicationModal');
    modal.classList.remove('active');
});

async function acceptApplication(applicationId, companionId) {
    console.log("applicationId: ",applicationId)
    try {
        await apiRequest(`/api/meet/${applicationId}/accept`, {
            method: "POST"
        });
        alert("신청을 수락했습니다.");
        loadApplications(companionId); // 목록 새로고침
        fetchCompanions(); // 전체 목록 새로고침
    } catch (error) {
        console.error("Error accepting application:", error);
        alert("신청 수락 중 문제가 발생했습니다.");
    }
}


async function rejectApplication(applicationId, companionId) {
    try {
        await apiRequest(`/api/meet/${applicationId}/reject`, {
            method: "PATCH"
        });
        alert("신청을 거절했습니다.");
        loadApplications(companionId);
        fetchCompanions();
    } catch (error) {
        console.error("Error rejecting application:", error);
        alert("신청 거절 중 문제가 발생했습니다.");
    }
}

async function handleWithdraw(companionId) {
    if (confirm('정말로 이 동행에서 탈퇴하시겠습니까?')) {
        try {
            await apiRequest(`/api/meet/${companionId}`, {method: "DELETE"});
            alert("동행에서 성공적으로 탈퇴했습니다.");
            fetchCompanions();
        } catch (error) {
            console.error("동행 취소 중 오류 발생:", error);
            alert("탈퇴 처리 중 문제가 발생했습니다.");
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 리뷰 작성 버튼 클릭 이벤트 등록
    document.body.addEventListener('click', (event) => {
        if (event.target.classList.contains('write-review-btn')) {
            const userId = event.target.dataset.id;
            const userName = event.target.dataset.name;
            const companionId = event.target.dataset['companion-id']; // 추가된 companionId 데이터 속성

            openReviewModal(userId, userName, companionId); // companionId 전달
        }
    });

    // 모달 닫기 이벤트
    document.getElementById('reviewModal').querySelector('.close-btn').addEventListener('click', () => {
        const reviewModal = document.getElementById('reviewModal');
        reviewModal.style.display = 'none'; // 리뷰 작성 모달 닫기
    });


    // 리뷰 작성 폼 제출 이벤트
    document.getElementById('reviewForm').addEventListener('submit', async (event) => {
        event.preventDefault();

        const reviewContent = document.getElementById('reviewContent').value;
        const reviewRating = document.getElementById('reviewRating').value;
        const revieweeId = document.getElementById('reviewModal').dataset.revieweeId;
        const companionId = document.getElementById('reviewModal').dataset.companionId; // companionId 사용

        if (!reviewContent || !reviewRating) {
            alert('내용과 평점을 입력해주세요.');
            return;
        }

        try {
            await submitReview({
                revieweeId: parseInt(revieweeId),
                content: reviewContent,
                rating: parseInt(reviewRating),
            });
            alert('리뷰가 작성되었습니다.');
            closeReviewModal();
        } catch (error) {
            console.error('리뷰 작성 오류:', error);
            alert('리뷰 작성에 실패했습니다.');
        }
    });
});

function openReviewModal(revieweeId, userName, companionId) {
    console.log("Review Modal Opened with:", {revieweeId, userName, companionId}); // 디버깅용 로그 추가
    const reviewModal = document.getElementById('reviewModal');
    reviewModal.dataset.revieweeId = revieweeId;
    reviewModal.dataset.companionId = companionId;
    reviewModal.style.display = 'block';
}


function closeReviewModal() {
    const reviewModal = document.getElementById('reviewModal');
    reviewModal.style.display = 'none';
    document.getElementById('reviewContent').value = '';
    document.getElementById('reviewRating').value = '5';
}

async function submitReview(reviewData) {
    try {
        console.log('Submitting review:', reviewData); // revieweeId 확인
        const response = await apiRequest('/api/reviews', {
            method: 'POST',
            body: JSON.stringify({
                revieweeId: reviewData.revieweeId,
                content: reviewData.content,
                rating: reviewData.rating,
            }),
        });

        return response; // API 호출 결과 반환
    } catch (error) {
        console.error('리뷰 작성 오류:', error);
        throw new Error('리뷰 작성 실패');
    }
}

