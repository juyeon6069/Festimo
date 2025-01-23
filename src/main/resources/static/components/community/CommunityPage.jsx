import React, { useState, useEffect } from 'react';
import DiscussionList from './DiscussionList';
import TopDiscussions from './TopDiscussions';
import RecommendedTopics from './RecommendedTopics';
import FestivalCalendar from './FestivalCalendar';

const CommunityPage = () => {
    const [posts, setPosts] = useState([]);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        fetch('/api/companions', {
            headers: {
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/json'
            }
        })
            .then(response => response.json())
            .then(data => setPosts(data))
            .catch(error => console.error('Error:', error));
    }, []);

    return (
        <div className="container mx-auto px-4 py-8 flex gap-8">
            {/* 왼쪽 메인 컨텐츠 */}
            <div className="flex-1">
                <DiscussionList posts={posts} />
            </div>

            {/* 오른쪽 사이드바 */}
            <div className="w-96 space-y-4">
                <TopDiscussions/>
                <RecommendedTopics/>
                <FestivalCalendar/>
            </div>
        </div>
    );
};

export default CommunityPage;