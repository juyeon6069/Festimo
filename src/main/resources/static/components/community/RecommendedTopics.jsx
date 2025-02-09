import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const RecommendedTopics = () => {
    const [popularTags, setPopularTags] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPopularTags = async () => {
            try {
                const token = localStorage.getItem('accessToken');
                const response = await fetch('/api/tags/popular', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (response.ok) {
                    const data = await response.json();
                    setPopularTags(data);
                }
            } catch (error) {
                console.error('태그 조회 실패:', error);
            }
        };

        fetchPopularTags();
    }, []);

    return (
        <div className="bg-white rounded-lg p-6 shadow-lg">
            <h2 className="text-2xl font-bold mb-4">이번 주 인기 태그 🔥</h2>
            <div className="flex flex-wrap gap-2">
                {popularTags.length > 0 ? (
                    popularTags.map((tag, index) => (
                        <button
                            key={index}
                            onClick={() => navigate(`/community?tag=${encodeURIComponent(tag.name)}`)}
                            className="px-4 py-2 bg-gray-50 hover:bg-gray-100 rounded-full text-gray-700 text-sm font-medium transition-colors"
                        >
                            {tag.name} ({tag.count})
                        </button>
                    ))
                ) : (
                    <div className="w-full text-center py-4 text-gray-500">
                        <p>이번 주에는 인기 태그가 없습니다.</p>
                        <p className="text-sm mt-1">게시글에 태그를 추가하여 첫 번째 인기 태그를 만들어보세요!</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default RecommendedTopics;