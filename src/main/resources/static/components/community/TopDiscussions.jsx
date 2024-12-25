import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

const TopDiscussions = () => {
    const [discussions, setDiscussions] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchTopPosts = async () => {
            try {
                const response = await fetch('/api/companions/top-weekly', {
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${localStorage.getItem('token')}`,
                    },
                });
                if (!response.ok) throw new Error('Failed to fetch top posts');
                const data = await response.json();
                setDiscussions(data || []); // 데이터가 없는 경우 빈 배열
            } catch (err) {
                console.error('Error:', err);
            } finally {
                setIsLoading(false); // 로딩 상태 해제
            }
        };

        fetchTopPosts();
    }, []);

    if (isLoading) {
        // 로딩 상태 표시
        return (
            <div className="bg-white rounded-lg p-6 shadow-lg">
                <h2 className="text-2xl font-bold mb-4">이번 주 인기 게시글 🔥</h2>
                <div className="space-y-6">
                    {[...Array(5)].map((_, index) => (
                        <div key={index} className="animate-pulse space-y-2">
                            <div className="h-5 bg-gray-200 rounded w-3/4"></div>
                            <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    // discussions 데이터가 비어있는 경우
    if (discussions.length === 0) {
        return (
            <div className="bg-white rounded-lg p-6 shadow-lg">
                <h2 className="text-2xl font-bold mb-4">이번 주 인기 게시글 🔥</h2>
                <p className="text-gray-600">이번 주에는 인기 게시글이 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg p-6 shadow-lg">
            <h2 className="text-2xl font-bold mb-4">이번 주 인기 게시글 🔥</h2>
            <div className="space-y-6">
                {/* 데이터가 있을 때만 표시 */}
                {discussions.slice(0, 3).map((post) => (
                    <Link
                        to={`/post/${post.id}`}
                        key={post.id}
                        className="block p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-all border border-gray-100"
                    >
                        <h3 className="text-lg font-medium text-gray-800 mb-1">{post.title}</h3>
                        <p className="text-gray-600 text-sm line-clamp-2 mb-1">
                            {post.content}
                        </p>
                        <span className="text-custom-purple font-semibold group-hover:underline">
                            자세히 보기 →
                        </span>
                    </Link>
                ))}
            </div>
        </div>
    );
};

export default TopDiscussions;
