// 상수 정의
const API_ENDPOINTS = {
    USERS: '/api/admin/users',
    POSTS: '/api/admin/posts',
    COMPANIONS: '/api/companions',
    REVIEWS: '/api/admin/reviews'
};

const PAGINATION = {
    DEFAULT_PAGE: 1,
    PAGE_SIZE: 10
};


// 상태 관리
const state = {
    members: {
        currentPage: PAGINATION.DEFAULT_PAGE,
        editingUserId: null
    },
    posts: {
        currentPage: PAGINATION.DEFAULT_PAGE
    },
    reviews: {
        currentPage: PAGINATION.DEFAULT_PAGE
    }
};

// 유틸리티 함수
const utils = {
    async fetchWithErrorHandling(url, options = {}) {
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });

            if (!response.ok) {
                if (response.status === 204) {  // No Content
                    return null;
                }
                const errorData = await response.json().catch(() => null);
                throw new Error(errorData?.message || `요청 실패 (${response.status})`);
            }

            return response.status !== 204 ? await response.json() : null;
        } catch (error) {
            console.error('API 요청 실패:', error);
            throw error;
        }
    },

    createTableRow(data, columns, actions, type = 'member') {
        const row = document.createElement('tr');

        // ID 설정 로직 수정
        let id;
        if (type === 'member') {
            id = data.userId;
        } else if (type === 'review') {
            id = data.reviewId;
        } else {
            id = data.id;  // post 케이스
        }
        row.setAttribute('data-id', id);

        const cells = columns.map(column =>
            `<td class="${column.class || ''}">${column.render(data)}</td>`
        );

        // 액션 버튼 생성 로직 수정
        const actionButtons = actions.map(action => {
            if (type === 'member') {
                return `<button onclick="memberManager.${action.text === '수정' ? 'editMember' : 'deleteMember'}(${id})">${action.text}</button>`;
            } else if (type === 'review') {
                return `<button onclick="reviewManager.deleteReview(${id})">${action.text}</button>`;
            } else {
                return `<button onclick="postManager.deletePost(${id})">${action.text}</button>`;
            }
        }).join('');

        row.innerHTML = [...cells, `<td>${actionButtons}</td>`].join('');
        return row;
    }
};

// 회원 관리 모듈
const memberManager = {
    async loadMembers() {
        try {
            const data = await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.USERS}?page=${state.members.currentPage - 1}&size=${PAGINATION.PAGE_SIZE}`);

            const memberTable = document.getElementById('member-table');
            memberTable.innerHTML = '';

            const columns = [
                { class: 'no', render: (member, index) => (state.members.currentPage - 1) * PAGINATION.PAGE_SIZE + index + 1 },
                { class: 'user-id', render: member => member.userId },
                { class: 'user-name', render: member => member.userName },
                { class: 'user-nickname', render: member => member.nickname },
                { class: 'user-email', render: member => member.email },
                { class: 'user-role', render: member => member.role },
                { class: 'user-created-date', render: member => {
                        const date = new Date(member.createdDate);
                        return date.toLocaleDateString('ko-KR', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                        });
                    }},
                { class: 'user-gender', render: member => member.gender === 'M' ? '남성' : '여성' }
            ];

            const actions = [
                { text: '수정' },
                { text: '삭제' }
            ];

            data.content.forEach((member, index) => {
                memberTable.appendChild(utils.createTableRow(member, columns.map(col => ({
                    ...col,
                    render: (data) => col.render(data, index)
                })), actions, 'member'));
            });

            this.updatePagination(data);
        } catch (error) {
            alert(`회원 목록 로드 실패: ${error.message}`);
        }
    },

    async updatePagination(data) {
        const totalPages = data.totalPages;
        const currentPage = state.members.currentPage;

        document.getElementById('member-current-page').textContent = currentPage;
        document.getElementById('member-prev-page').disabled = currentPage === 1;
        document.getElementById('member-next-page').disabled = currentPage >= totalPages;
    },

    async editMember(userId) {
        const row = document.querySelector(`tr[data-id="${userId}"]`);
        if (!row) return;

        state.members.editingUserId = userId;
        const editSection = document.getElementById('edit-section');

        ['name', 'nickname', 'email', 'role'].forEach(field => {
            const value = row.querySelector(`.user-${field}`).textContent;
            document.getElementById(`edit-${field}`).value = value;
        });

        document.getElementById('edit-gender').value =
            row.querySelector('.user-gender').textContent === '남성' ? 'M' : 'F';

        editSection.style.display = 'block';
    },

    async updateMember(event) {
        event.preventDefault();

        const updatedUser = {
            userName: document.getElementById('edit-name').value,
            nickname: document.getElementById('edit-nickname').value,
            email: document.getElementById('edit-email').value,
            gender: document.getElementById('edit-gender').value,
            ratingAvg: 5
        };

        try {
            await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.USERS}/${state.members.editingUserId}`,
                {
                    method: 'PUT',
                    body: JSON.stringify(updatedUser)
                }
            );

            alert('회원 정보가 수정되었습니다.');
            this.cancelEdit();
            this.loadMembers();
        } catch (error) {
            alert(`수정 실패: ${error.message}`);
        }
    },

    async deleteMember(userId) {
        if (!confirm('정말로 삭제하시겠습니까?')) return;

        try {
            await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.USERS}/${userId}`,
                { method: 'DELETE' }
            );

            alert('회원이 삭제되었습니다.');
            this.loadMembers();
        } catch (error) {
            alert(`삭제 실패: ${error.message}`);
        }
    },

    cancelEdit() {
        document.getElementById('edit-section').style.display = 'none';
        state.members.editingUserId = null;
    },

    handlePagination(direction) {
        if (direction === 'prev' && state.members.currentPage > 1) {
            state.members.currentPage--;
        } else if (direction === 'next') {
            state.members.currentPage++;
        }
        this.loadMembers();
    }
};

// 게시글 관리 모듈
const postManager = {
    async loadPosts() {
        try {
            const data = await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.COMPANIONS}?page=${state.posts.currentPage}&size=${PAGINATION.PAGE_SIZE}`
            );

            console.log('Posts data:', data);

            const postTable = document.getElementById('post-table');
            postTable.innerHTML = '';

            // 데이터가 없거나 content 배열이 없는 경우 처리
            if (!data || !data.content) {
                postTable.innerHTML = '<tr><td colspan="5" class="text-center">게시글이 없습니다.</td></tr>';
                // 페이지네이션 업데이트도 호출해야 함
                this.updatePagination(data);
                return;
            }

            const columns = [
                { render: post => post.id },  // companionId 대신 id 사용
                { render: post => post.title },
                { render: post => post.writer },
                { render: post => post.views }
            ];

            const actions = [
                { text: '삭제' }
            ];

            data.content.forEach(post => {
                console.log('Processing post:', post);
                postTable.appendChild(utils.createTableRow(post, columns, actions, 'post'));
            });

            this.updatePagination(data);
        } catch (error) {
            alert(`게시글 목록 로드 실패: ${error.message}`);
            // 에러 발생 시에도 테이블을 비우고 페이지네이션 비활성화
            const postTable = document.getElementById('post-table');
            postTable.innerHTML = '<tr><td colspan="5" class="text-center">게시글 로드 중 오류가 발생했습니다.</td></tr>';
            this.updatePagination(null);
        }
    },

    async updatePagination(data) {
        document.getElementById('current-page').textContent = state.posts.currentPage;
        document.getElementById('prev-page').disabled = state.posts.currentPage === 1;
        document.getElementById('next-page').disabled = data.content.length < PAGINATION.PAGE_SIZE;
    },

    async deletePost(postId) {
        console.log('Deleting post with ID:', postId);

        if (!postId) {
            alert('게시글 ID가 유효하지 않습니다.');
            return;
        }

        if (!confirm('정말로 삭제하시겠습니까?')) return;

        try {
            await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.POSTS}/${postId}`,
                { method: 'DELETE' }
            );

            alert('게시글이 삭제되었습니다.');
            this.loadPosts();
        } catch (error) {
            alert(`삭제 실패: ${error.message}`);
        }
    },

    handlePagination(direction) {
        if (direction === 'prev' && state.posts.currentPage > 1) {
            state.posts.currentPage--;
        } else if (direction === 'next') {
            state.posts.currentPage++;
        }
        this.loadPosts();
    }
};



// 리뷰 관리 모듈
const reviewManager = {
    async loadReviews() {
        try {
            const data = await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.REVIEWS}?page=${state.reviews.currentPage-1}&size=${PAGINATION.PAGE_SIZE}`
            );

            const reviewTable = document.getElementById('review-table');
            reviewTable.innerHTML = '';

            // 데이터가 없거나 content 배열이 없는 경우 처리
            if (!data || !data.content) {
                reviewTable.innerHTML = '<tr><td colspan="7" class="text-center">리뷰가 없습니다.</td></tr>';
                // 페이지네이션 업데이트도 호출
                this.updatePagination(data);
                return;
            }

            const columns = [
                { render: review => review.reviewId },
                { render: review => review.reviewerId },
                { render: review => review.revieweeId },
                { render: review => `${review.rating}점` },
                { render: review => review.content },
                { render: review => {
                        const date = new Date(review.createdAt);
                        return date.toLocaleDateString('ko-KR', {
                            year: 'numeric',
                            month: '2-digit',
                            day: '2-digit',
                            hour: '2-digit',
                            minute: '2-digit'
                        });
                    }}
            ];

            const actions = [
                { text: '삭제' }
            ];

            data.content.forEach(review => {
                reviewTable.appendChild(utils.createTableRow(review, columns, actions, 'review'));
            });

            this.updatePagination(data);
        } catch (error) {
            // 404 에러는 데이터가 없는 것으로 처리
            if (error.message.includes('404')) {
                const reviewTable = document.getElementById('review-table');
                reviewTable.innerHTML = '<tr><td colspan="7" class="text-center">리뷰가 없습니다.</td></tr>';
                this.updatePagination(null);
                return;
            }

            // 다른 에러는 기존대로 처리
            alert(`리뷰 목록 로드 실패: ${error.message}`);
            const reviewTable = document.getElementById('review-table');
            reviewTable.innerHTML = '<tr><td colspan="7" class="text-center">리뷰 로드 중 오류가 발생했습니다.</td></tr>';
            this.updatePagination(null);
        }
    },

    async updatePagination(data) {
        const totalPages = data?.totalPages || 0;
        const currentPage = state.reviews.currentPage;

        const currentPageEl = document.getElementById('review-current-page');
        const prevPageEl = document.getElementById('review-prev-page');
        const nextPageEl = document.getElementById('review-next-page');

        // 현재 페이지 표시
        currentPageEl.textContent = currentPage;

        // 데이터가 없거나 오류 상태일 경우
        if (!data || !data.content) {
            prevPageEl.disabled = true;
            nextPageEl.disabled = true;
            return;
        }

        // 이전 페이지 버튼 상태 설정
        prevPageEl.disabled = currentPage === 1;

        // 다음 페이지 버튼 상태 설정
        nextPageEl.disabled = currentPage >= totalPages;
    },

    async deleteReview(reviewId) {
        if (!confirm('정말로 이 리뷰를 삭제하시겠습니까?')) return;

        try {
            await utils.fetchWithErrorHandling(
                `${API_ENDPOINTS.REVIEWS}/${reviewId}`,
                { method: 'DELETE' }
            );

            alert('리뷰가 삭제되었습니다.');
            this.loadReviews();
        } catch (error) {
            alert(`삭제 실패: ${error.message}`);
        }
    },

    handlePagination(direction) {
        if (direction === 'prev' && state.reviews.currentPage > 1) {
            state.reviews.currentPage--;
        } else if (direction === 'next') {
            state.reviews.currentPage++;
        }
        this.loadReviews();
    }
};


// 이벤트 리스너 설정
document.addEventListener('DOMContentLoaded', () => {
    // 탭 전환 이벤트 설정
    const tabs = document.querySelectorAll('.tab');
    const tabContents = document.querySelectorAll('.tab-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));

            tab.classList.add('active');
            const activeTabContent = document.getElementById(`${tab.id}-management`);
            activeTabContent.classList.add('active');

            if (tab.id === 'member') {
                memberManager.loadMembers();
            } else if (tab.id === 'post') {
                postManager.loadPosts();
            }else if (tab.id === 'review') {
                reviewManager.loadReviews();
            }
        });
    });

    // 회원 관리 페이지네이션 이벤트
    document.getElementById('member-prev-page').addEventListener('click', () =>
        memberManager.handlePagination('prev'));
    document.getElementById('member-next-page').addEventListener('click', () =>
        memberManager.handlePagination('next'));

    // 게시글 관리 페이지네이션 이벤트
    document.getElementById('prev-page').addEventListener('click', () =>
        postManager.handlePagination('prev'));
    document.getElementById('next-page').addEventListener('click', () =>
        postManager.handlePagination('next'));

    // 회원 수정 폼 이벤트
    document.getElementById('edit-form').addEventListener('submit', (event) =>
        memberManager.updateMember(event));
    document.querySelector('#edit-section button[type="button"]').addEventListener('click', () =>
        memberManager.cancelEdit());

    //리뷰 관리 페이지네이션 이벤트
    document.getElementById('review-prev-page').addEventListener('click', () =>
        reviewManager.handlePagination('prev'));
    document.getElementById('review-next-page').addEventListener('click', () =>
        reviewManager.handlePagination('next'));

    // 초기 데이터 로드
    memberManager.loadMembers();
});