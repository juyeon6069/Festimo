import React, {useEffect, useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';

const Navigation = ({isLoggedIn, handleLogout}) => {
    const navItems = [
        { title: 'Home', path: '/' },
        { title: 'Mypage', path: '/html/mypage.html' },
        { title: 'Festival', path: '/html/festival.html' },
        { title: 'Community', path: '/community' },
        { title: 'Companion', path: '/html/companion.html' },
    ];

    return (
        <div className="hidden md:flex items-center gap-8 text-[14.5px] translate-y-[2px]">
            {navItems.map((item, index) => (
                <div key={index}>
                    <a
                        href={item.path}
                        className="text-[#4a4a4a] hover:text-[#4a4a4a]/80 leading-[1.2]"
                    >
                        <span>{item.title}</span>
                    </a>
                </div>
            ))}
            {isLoggedIn ? (
                <button
                    onClick={handleLogout}
                    className="text-[#4a4a4a] hover:text-[#4a4a4a]/80 leading-[1.2]"
                >
                    Logout
                </button>
            ) : (
                <>
                    <a
                        href="/html/login.html"
                        className="text-[#4a4a4a] hover:text-[#4a4a4a]/80 leading-[1.2]"
                    >
                        Login
                    </a>
                    <a
                        href="html/registration_form.html"
                        className="px-4 py-[0.5rem] bg-[#5c5d8d] text-white rounded-[3px] hover:shadow-md hover:shadow-black/30 transition-all leading-[1.2]"
                    >
                        Get started
                    </a>
                </>
            )}
        </div>
    );
};

const Header = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        setIsLoggedIn(!!token);
    }, []);

    const handleLogout = async () => {
        const refreshToken = localStorage.getItem('accessToken');

        try {
            const response = await fetch('/api/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${refreshToken}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.status === 401) {
                // 토큰 정상 삭제 되는데도 401 반환해 일단 정상 처리로 간주함
                console.warn('Token was already invalid or expired.');
            }

            if (response.ok || response.status === 401) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('userInfo');
                alert('로그아웃되었습니다.');
                setIsLoggedIn(false);
                navigate('/');
            } else {
                throw new Error('Unexpected response.');
            }
        } catch (error) {
            console.error('Error during logout:', error);
            alert('로그아웃 중 문제가 발생했습니다. 다시 시도해주세요.');
        }
    };


    return (
        <header
            style={{padding: '1rem 2rem'}}
            className="flex items-center justify-between bg-white text-[14.5px] font-[Verdana,Geneva,Tahoma,sans-serif]"
        >
            <div className="flex items-center">
                <Link to="/" className="flex items-center">
                    <img
                        src="/imgs/festimoLogo.svg"
                        alt="Festimo Logo"
                        className="w-[140px] h-auto"
                    />
                </Link>
            </div>

            <Navigation isLoggedIn={isLoggedIn} handleLogout={handleLogout}/>
        </header>
    );
};

export default Header;