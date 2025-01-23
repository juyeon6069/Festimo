import { apiRequest } from './apiClient.js'; // 공통 모듈 불러오기

// 사용자 정보를 가져와 폼에 채우는 함수
async function fetchAndPopulateUserData() {
    try {
        const data = await apiRequest('/api/user/mypage', { method: 'GET' }); // authentication에서 이메일 사용
        document.getElementById('username').value = data.userName; // 백엔드 필드와 맞춤
        document.getElementById('nickname').value = data.nickname;
        document.getElementById('gender').value = data.gender;
    } catch (error) {
        console.error('Error fetching user data:', error);
        alert('사용자 정보를 불러오는데 실패했습니다.');
    }
}

// 사용자 정보 수정 요청 함수
async function submitProfileUpdate(event) {
    event.preventDefault();

    const updatedData = {
        userName: document.getElementById('username').value,
        nickname: document.getElementById('nickname').value,
        gender: document.getElementById('gender').value
    };

    try {
        await apiRequest('/api/user/mypage/update', { // 새로운 경로 사용
            method: 'PUT',
            body: JSON.stringify(updatedData),
        });

        alert('회원정보가 성공적으로 수정되었습니다.');
        window.location.href = '/html/mypage.html';
    } catch (error) {
        console.error('Error updating profile:', error);

        if (error.data && error.data.errorCode) {
            switch (error.data.errorCode) {
                case 'DUPLICATE_NICKNAME':
                    alert('중복된 닉네임입니다.');
                    break;
                case 'INVALID_GENDER':
                    alert('성별 값이 올바르지 않습니다.');
                    break;
                default:
                    alert('알 수 없는 오류가 발생했습니다.');
            }
        } else {
            alert('서버와 통신 중 문제가 발생했습니다.');
        }
    }
}

// 비밀번호 변경 페이지로 이동
function goToPasswordChangePage() {
    window.location.href = '/html/password-change.html';
}

// 회원 탈퇴 요청
async function deleteAccount() {
    if (!confirm('정말로 회원 탈퇴를 진행하시겠습니까?')) return;

    try {
        await apiRequest('/api/user/mypage/delete', { method: 'DELETE' }); // authentication 사용 경로

        // Access Token 제거
        localStorage.removeItem('accessToken'); // Access Token 삭제
        // Refresh Token도 제거
        document.cookie = 'refreshToken=; Max-Age=0; path=/;';

        alert('회원 탈퇴가 완료되었습니다.');
        window.location.href = '/html/registration_form.html'; // 탈퇴 후 회원가입 페이지로 이동
    } catch (error) {
        console.error('Error deleting account:', error);
        alert('회원 탈퇴 중 문제가 발생했습니다.');
    }
}

// 이벤트 리스너 등록
document.addEventListener('DOMContentLoaded', fetchAndPopulateUserData);
document.getElementById('edit-profile-form').addEventListener('submit', submitProfileUpdate);
document.getElementById('password-change-btn').addEventListener('click', goToPasswordChangePage);
document.getElementById('delete-account-btn').addEventListener('click', deleteAccount);
