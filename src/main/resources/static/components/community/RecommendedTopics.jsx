import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const RecommendedTopics = () => {
    const [popularTags, setPopularTags] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchPopularTags = async () => {
            try {
                const response = await fetch('/api/tags/popular');
                if (!response.ok) throw new Error('Failed to fetch popular tags');
                const data = await response.json();
                console.log('Fetched tags:', data); // 데이터 확인용 로그
                setPopularTags(data);
            } catch (error) {
                console.error('Error:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchPopularTags();
    }, []);

    const handleTagClick = (tagName) => {
        if (tagName) {  // tagName이 undefined가 아닌 경우에만 처리
            navigate(`/community?tag=${encodeURIComponent(tagName)}`);
        }
    };

    return (
        <div className="bg-white rounded-lg p-6 shadow-lg">
            <div className="flex items-center gap-2 mb-4">
                <h2 className="text-2xl font-bold mb-4">이번 주 인기 태그 🔥</h2>
            </div>
            <div className="flex flex-wrap gap-2">
                {popularTags.map((tag, index) => (
                    <button
                        key={index}
                        onClick={() => handleTagClick(tag.name)}
                        className="px-4 py-2 bg-gray-50 hover:bg-gray-100 rounded-full text-gray-700 text-sm font-medium transition-colors"
                    >
                        {tag.name} ({tag.count})
                    </button>
                ))}
            </div>
        </div>
    );
};

export default RecommendedTopics;