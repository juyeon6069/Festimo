import { apiRequest } from './apiClient.js'; // 공통 모듈 불러오기

document.getElementById('password-change-form').addEventListener('submit', async (event) => {
    event.preventDefault(); // 기본 폼 제출 동작 방지

    // 폼 데이터 가져오기
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;

    const requestData = {
        oldPassword,
        newPassword,
    };

    try {
        // API 요청 보내기
        await apiRequest('/api/user/mypage/change-password', {
            method: 'POST',
            body: JSON.stringify(requestData),
        });

        // 성공 처리
        alert('비밀번호가 성공적으로 변경되었습니다.');
        window.location.href = '/html/mypage.html';
    } catch (error) {
        console.error('Error:', error);

        // ✅ 에러 메시지 처리 로직
        if (error.data && error.data.errorCode) {
            // 서버에서 전달된 에러 메시지 처리
            const errorCode = error.data.errorCode;
            const errorMessage = error.data.error || '알 수 없는 오류가 발생했습니다.';

            switch (errorCode) {
                case 'INVALID_OLD_PASSWORD':
                    alert('기존 비밀번호가 올바르지 않습니다.');
                    break;
                case 'PASSWORD_CANNOT_BE_NULL':
                    alert('비밀번호를 입력해주세요.');
                    break;
                case 'PASSWORD_INVALID_LENGTH':
                    alert('비밀번호는 8~20자여야 합니다.');
                    break;
                case 'PASSWORD_MISSING_LETTER':
                    alert('비밀번호에는 적어도 하나의 문자가 포함되어야 합니다.');
                    break;
                case 'PASSWORD_MISSING_NUMBER':
                    alert('비밀번호에는 적어도 하나의 숫자가 포함되어야 합니다.');
                    break;
                case 'PASSWORD_MISSING_SPECIAL_CHARACTER':
                    alert('비밀번호에는 적어도 하나의 특수문자가 포함되어야 합니다.');
                    break;
                default:
                    alert(errorMessage);
            }
        } else {
            // ✅ JSON 파싱 실패 또는 기타 오류 발생 시 기본 메시지
            alert(error.message || '서버와 통신 중 문제가 발생했습니다.');
        }
    }
});


