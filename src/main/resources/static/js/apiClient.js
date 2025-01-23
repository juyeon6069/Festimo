// apiClient.js

let accessToken = localStorage.getItem('accessToken'); // LocalStorage에서 Access Token 가져오기

/**
 * Access Token 만료 여부 확인
 * @param {string} token - JWT Access Token
 * @returns {boolean} - 토큰 만료 여부
 */
function isTokenExpired(token) {
    if (!token) return true; // 토큰이 없으면 만료된 것으로 간주
    const [, payload] = token.split('.');
    const { exp } = JSON.parse(atob(payload)); // 토큰의 만료 시간 가져오기
    return exp * 1000 < Date.now(); // 현재 시간과 비교
}

/**
 * Access Token 갱신
 * @returns {Promise<string>} - 새로 발급된 Access Token
 */
async function refreshAccessToken() {
    console.log("Refreshing Access Token...");
    const response = await fetch('http://localhost:8080/api/refresh', {
        method: 'POST',
        credentials: 'include', // Refresh Token은 HttpOnly 쿠키로 전달
    });

    if (!response.ok) {
        console.error("Failed to refresh Access Token, response:", response);
        throw new Error('Failed to refresh token');
    }

    const data = await response.json();
    console.log("Refreshed Access Token:", data.accessToken);
    accessToken = data.accessToken; // 새로운 Access Token 저장
    localStorage.setItem('accessToken', accessToken); // LocalStorage에 저장
    return accessToken;
}

/**
 * API 요청
 * @param {string} url - API URL
 * @param {object} options - fetch API 옵션
 * @returns {Promise<any>} - API 응답 데이터
 */
async function apiRequest(url, options = {}) {
    console.log("API Request initiated:", url);
    if (!accessToken) {
        console.error("Access Token missing. User needs to log in.");
        throw new Error('Require Login');
    } else if (isTokenExpired(accessToken)) {
        try {
            console.log("Access Token expired. Refreshing...");
            await refreshAccessToken(); // Access Token 만료 시 갱신
        } catch (error) {
            console.error("Unable to refresh token:", error);
            throw new Error('Unauthorized'); // 토큰 갱신 실패 시 예외 처리
        }
    }

    // Authorization 헤더 추가
    options.headers = {
        ...options.headers,
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
    };

    console.log("Headers before request:", options.headers);

    const response = await fetch(url, options);

    if (!response.ok) {
        let errorData = {};
        try {
            errorData = await response.json();
        } catch (parseError) {
            console.error("Error parsing server response:", parseError);
        }

        throw {
            status: response.status,
            data: errorData,
            message: `HTTP Error ${response.status}: ${response.statusText}`,
        };
    }

    // 응답이 JSON인지 확인
    const contentType = response.headers.get('Content-Type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    } else {
        return response.text();
    }
}



// 모듈 내보내기
export { apiRequest, refreshAccessToken, isTokenExpired };
