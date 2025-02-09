fetch('../html/header.html')
    .then(response => response.text())
    .then(data => {
        document.getElementById('header-container').innerHTML = data;

        // 헤더가 로드된 후 자바스크립트 실행
        const accessToken = localStorage.getItem('accessToken');
        const loginLogoutLink = document.getElementById('login-logout-link');
        const getStartedLink = document.getElementById('getStarted-link');

        if (accessToken) {
            loginLogoutLink.textContent = 'Logout';
            loginLogoutLink.href = 'http://localhost:8080/api/logout';
            loginLogoutLink.addEventListener('click',async function(event) {
                event.preventDefault();

                try{
                    const response = await fetch('http://localhost:8080/api/logout',{
                        method:'POST',
                        credentials:'include',
                        headers: {
                            'Authorization': `Bearer ${accessToken}`, // Authorization 헤더 추가
                            'Content-Type': 'application/json'
                        }
                    })
                        .then(response =>{
                            if(response.ok){
                                alert('로그아웃 되었습니다.');
                                localStorage.removeItem('accessToken');
                                window.location.href='/'
                            }
                            else{
                                alert('로그아웃에 실패했습니다. 다시 시도해 주세요.');
                            }
                        })
                }catch (error){
                    console.error('로그아웃 오류 : ',error);
                    alert('로그아웃에 실패했습니다. 다시 시도해 주세요.');
                }
            });
            getStartedLink.style.display = 'none';
        } else {
            loginLogoutLink.textContent = 'Login';
            loginLogoutLink.href = '/html/login.html';
            getStartedLink.style.display = 'inline-block';
        }
    })
    .catch(error => console.error('Error loading header:', error));