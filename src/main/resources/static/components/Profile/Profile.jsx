import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './Profile.css';
import Pagination from '../common/Pagination';

const Profile = () => {
    const { mail } = useParams();
    const userEmail = mail;
    const navigate = useNavigate();

    const [profile, setProfile] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [followersCount, setFollowersCount] = useState(0);
    const [followingCount, setFollowingCount] = useState(0);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [modalOpen, setModalOpen] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [modalData, setModalData] = useState([]);
    const [isFollowing, setIsFollowing] = useState(false);

    const currentUser = JSON.parse(localStorage.getItem("userInfo") || "{}");
    console.log("Current user:", currentUser);


    // 받은 리뷰를 불러오는 함수 (페이지 번호 포함)
    const fetchReviews = (userId, page) => {
        fetch(`/api/reviews/reviewee/${userId}/paged?page=${page - 1}&size=5&sortBy=createdAt&sortDir=desc`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching reviews');
                }
                return response.json();
            })
            .then(reviewData => {
                setReviews(reviewData.content);
                setTotalPages(reviewData.totalPages); // API에서 총 페이지 수 제공
            })
            .catch(err => console.error("Error fetching reviews", err));
    };

    const fetchFollowStatus = (profileId) => {
        if (!currentUser || !currentUser.id || !profileId) {
            console.warn("팔로우 상태 확인을 위한 정보가 부족합니다.");
            return;
        }
        fetch(`/api/follow/check?followerId=${currentUser.id}&followeeId=${profileId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error checking follow status');
                }
                return response.json();
            })
            .then(status => {
                setIsFollowing(status);
            })
            .catch(err => console.error("Error checking follow status", err));
    };


    // 팔로워 목록 불러오기 (모달용)
    const fetchFollowersList = (userId) => {
        fetch(`/api/follow/followers/${userId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching followers list');
                }
                return response.json();
            })
            .then(data => {
                setModalData(data);
                setModalTitle('팔로워 목록');
                setModalOpen(true);
            })
            .catch(err => console.error("Error fetching followers list", err));
    };

    // 팔로잉 목록 불러오기 (모달용)
    const fetchFollowingList = (userId) => {
        fetch(`/api/follow/following/${userId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching following list');
                }
                return response.json();
            })
            .then(data => {
                setModalData(data);
                setModalTitle('팔로잉 목록');
                setModalOpen(true);
            })
            .catch(err => console.error("Error fetching following list", err));
    };

    useEffect(() => {
        // 회원 정보 조회 (fetch 사용)
        fetch(`/api/user/${userEmail}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error fetching user');
                }
                return response.json();
            })
            .then(userData => {
                console.log("Fetched user data:", userData);
                setProfile(userData);
                const userId = userData.id;

                // 팔로워 수 조회 (fetch 사용)
                fetch(`/api/follow/followers/count?userId=${userId}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error fetching followers count');
                        }
                        return response.json();
                    })
                    .then(data => setFollowersCount(data))
                    .catch(err => console.error("Error fetching followers count", err));

                // 팔로잉 수 조회 (fetch 사용)
                fetch(`/api/follow/following/count?userId=${userId}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Error fetching following count');
                        }
                        return response.json();
                    })
                    .then(data => setFollowingCount(data))
                    .catch(err => console.error("Error fetching following count", err));

                // 초기 받은 리뷰 조회 (페이지 1)
                fetchReviews(userId, 1);

                fetchFollowStatus(userId);
            })
            .catch(err => console.error("Error fetching user", err));
    }, [userEmail]);



    // 페이지 변경 핸들러
    const handlePageChange = (page) => {
        setCurrentPage(page);
        if (profile && profile.id) {
            fetchReviews(profile.id, page);
        }
    };

    // 팔로우 요청 함수 (POST)
    const handleFollow = () => {
        console.log("handleFollow: currentUser=", currentUser, "profile=", profile);
        const token = localStorage.getItem("accessToken");
        fetch(`/api/follow`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                followerId: currentUser.id,
                followeeId: profile.id
            })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('팔로우 요청에 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                setIsFollowing(true);
                setFollowersCount(prev => prev + 1);
            })
            .catch(err => {
                console.error("Error following user:", err);
                alert(err.message);
            });
    };

    // 언팔로우 요청 함수 (DELETE)
    const handleUnfollow = () => {
        const token = localStorage.getItem("accessToken");
        fetch(`/api/follow?followerId=${currentUser.id}&followeeId=${profile.id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('언팔로우 요청에 실패했습니다.');
                }
                return response.text();
            })
            .then(() => {
                setIsFollowing(false);
                setFollowersCount(prev => prev - 1);
            })
            .catch(err => {
                console.error("Error unfollowing user:", err);
                alert(err.message);
            });
    };


    // 모달 닫기 핸들러
    const closeModal = () => {
        setModalOpen(false);
        setModalData([]);
    };

    // 모달 목록 항목 클릭 시 해당 유저의 프로필 페이지로 이동하는 핸들러
    const handleUserClick = (userEmail) => {
        // 모달 닫고, 해당 사용자의 프로필 페이지로 이동
        setModalOpen(false);
        navigate(`/profile/${userEmail}`);
    };

    if (!profile) {
        return <div>Loading...</div>;
    }

    //현재 로그인 사용자가 본인 프로필이면 팔로우 버튼 미노출
    const showFollowButton = currentUser.id && currentUser.id !== profile.id;

    return (
        <div className="profile-page">
            <div className="profile-header">
                <div className="profile-image">
                    <img
                        src={profile.avatar || "/imgs/default-avatar.png"}
                        alt="Profile"
                    />
                </div>
                <div className="profile-info">
                    <h2 className="user-name">{profile.userName}</h2>
                    <div className="profile-stats">
                        <div className="stat-item">
                            <span className="stat-label">평점</span>
                            <span className="stat-value">
                                {profile.ratingAvg ? profile.ratingAvg.toFixed(1) : '0.0'}
                            </span>
                        </div>
                        <div className="stat-item" onClick={() => fetchFollowingList(profile.id)} style={{ cursor: 'pointer' }}>
                            <span className="stat-label">팔로잉</span>
                            <span className="stat-value">{followingCount}</span>
                        </div>
                        <div className="stat-item" onClick={() => fetchFollowersList(profile.id)} style={{ cursor: 'pointer' }}>
                            <span className="stat-label">팔로워</span>
                            <span className="stat-value">{followersCount}</span>
                        </div>
                    </div>
                    {showFollowButton && (
                        <div className="follow-btn-container">
                            {isFollowing ? (
                                <button onClick={handleUnfollow} className="follow-btn unfollow">
                                    팔로우 끊기
                                </button>
                            ) : (
                                <button onClick={handleFollow} className="follow-btn follow">
                                    팔로우 하기
                                </button>
                            )}
                        </div>
                    )}
                </div>
            </div>


            <div className="reviews-section">
                <h3>받은 리뷰</h3>
                <div className="reviews-list">
                    {reviews.length === 0 ? (
                        <p>받은 리뷰가 없습니다.</p>
                    ) : (
                        reviews.map((review) => (
                            <div className="review-item" key={review.id}>
                                <div className="review-header">
                                    <span className="review-rating">
                                        {'★'.repeat(review.rating) + '☆'.repeat(5 - review.rating)}
                                    </span>
                                    <span className="review-date">
                                        {new Date(review.createdAt).toLocaleDateString()}
                                    </span>
                                </div>
                                <div className="review-content">
                                    <p>{review.content}</p>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
            </div>

            {/* 모달 렌더링 */}
            {modalOpen && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h3>{modalTitle}</h3>
                        <ul className="modal-list">
                            {modalData.length > 0 ? (
                                modalData.map((user, index) => (
                                    <li
                                        key={index}
                                        onClick={() => handleUserClick(user.email)}
                                        style={{ cursor: 'pointer' }}
                                    >
                                        {user.nickname}
                                    </li>
                                ))
                            ) : (
                                <li>표시할 사용자가 없습니다.</li>
                            )}
                        </ul>
                        <button className="modal-close-btn" onClick={closeModal}>닫기</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Profile;
