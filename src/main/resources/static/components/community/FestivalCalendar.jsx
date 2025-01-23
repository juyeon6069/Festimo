import React, { useState, useEffect } from 'react';
import Calendar from 'react-calendar';
import "react-calendar/dist/Calendar.css";
import { useNavigate } from 'react-router-dom';

const FestivalCalendar = () => {
    const navigate = useNavigate();
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [festivals, setFestivals] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedDateFestivals, setSelectedDateFestivals] = useState([]);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleFestivalClick = (festivalId) => {
        window.location.href = `/html/festival_detail.html?festival_id=${festivalId}`;
    };

    const fetchFestivalsByMonth = async (year, month) => {
        setLoading(true);
        try {
            const response = await fetch(`/api/events/filter/month?year=${year}&month=${month}&page=0&size=100`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setFestivals(data.content || []);
        } catch (error) {
            console.error("Error fetching festivals:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleDateChange = (date) => {
        setSelectedDate(date);
        const formattedDate = date.toISOString().split('T')[0];
        const eventsOnDate = festivals.filter((festival) => {
            const startDate = new Date(festival.startDate).toISOString().split('T')[0];
            const endDate = new Date(festival.endDate).toISOString().split('T')[0];
            return startDate <= formattedDate && endDate >= formattedDate;
        });
        setSelectedDateFestivals(eventsOnDate);
    };

    const renderTileContent = ({ date }) => {
        const formattedDate = date.toISOString().split('T')[0];
        const eventsOnDate = festivals.filter((festival) => {
            const startDate = new Date(festival.startDate).toISOString().split('T')[0];
            const endDate = new Date(festival.endDate).toISOString().split('T')[0];
            return startDate <= formattedDate && endDate >= formattedDate;
        });

        return eventsOnDate.length > 0 ? (
            <div className="text-xs mt-1">
                <div className="h-1 w-1 bg-custom-purple rounded-full mx-auto"></div>
                <div className="text-custom-purple mt-1">{eventsOnDate.length}개</div>
            </div>
        ) : null;
    };

    const renderFestivalModal = () => {
        if (!isModalOpen) return null;

        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                <div className="bg-white rounded-lg p-6 w-11/12 max-w-2xl max-h-[80vh] overflow-y-auto">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-semibold text-custom-purple">
                            {selectedDate.toLocaleDateString('ko-KR')}의 모든 축제
                        </h3>
                        <button onClick={() => setIsModalOpen(false)} className="text-gray-500 hover:text-gray-700">
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                    <div className="space-y-3">
                        {selectedDateFestivals.map((festival) => (
                            <div
                                key={festival.festival_id}
                                onClick={() => handleFestivalClick(festival.festival_id)}
                                className="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 cursor-pointer"
                            >
                                <h4 className="font-medium text-custom-purple">{festival.title}</h4>
                                <p className="text-sm text-gray-600 mt-1">{festival.address}</p>
                                <div className="text-xs text-gray-500 mt-1">
                                    {new Date(festival.startDate).toLocaleDateString()} ~
                                    {new Date(festival.endDate).toLocaleDateString()}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        );
    };

    const renderSelectedFestivals = () => {
        if (loading) {
            return <div className="text-center py-4 text-gray-500">로딩 중...</div>;
        }

        if (selectedDateFestivals.length === 0) {
            return <div className="text-center py-4 text-gray-500">이 날짜에 예정된 축제가 없습니다.</div>;
        }

        const displayFestivals = selectedDateFestivals.slice(0, 5);
        const remainingCount = selectedDateFestivals.length - 5;

        return (
            <div className="flex flex-wrap gap-2">
                {displayFestivals.map((festival) => (
                    <div
                        key={festival.festival_id}
                        onClick={() => handleFestivalClick(festival.festival_id)}
                        className="inline-flex px-3 py-1.5 bg-gray-50 text-sm text-gray-700 rounded-full
                                hover:bg-custom-purple hover:text-white transition-colors cursor-pointer"
                    >
                        {festival.title}
                    </div>
                ))}
                {remainingCount > 0 && (
                    <div
                        onClick={() => setIsModalOpen(true)}
                        className="inline-flex px-3 py-1.5 bg-gray-100 text-sm text-gray-600 rounded-full cursor-pointer hover:bg-gray-200"
                    >
                        +{remainingCount}개 더보기
                    </div>
                )}
            </div>
        );
    };

    useEffect(() => {
        const year = selectedDate.getFullYear();
        const month = selectedDate.getMonth() + 1;
        fetchFestivalsByMonth(year, month);
    }, [selectedDate]);

    useEffect(() => {
        // 초기 로딩 시 현재 날짜의 축제 데이터 설정
        handleDateChange(selectedDate);
    }, [festivals]);

    return (
        <div className="max-w-7xl mx-auto bg-white rounded-lg shadow-lg">
            <div className="p-8">
                <h2 className="text-2xl font-bold mb-6 text-custom-purple text-center">축제 달력</h2>
                <div className="mb-8">
                    <Calendar
                        onChange={handleDateChange}
                        value={selectedDate}
                        tileContent={renderTileContent}
                        className="w-full shadow-md rounded-lg"
                        minDate={new Date(new Date().getFullYear() - 3, 0, 1)}
                        maxDate={new Date(new Date().getFullYear() + 3, 11, 31)}
                    />
                </div>
                <div className="mt-8">
                    <h3 className="text-xl font-semibold text-custom-purple mb-4 flex items-center">
                        <span className="bg-custom-purple w-2 h-6 mr-2 rounded"></span>
                        {selectedDate.toLocaleDateString('ko-KR', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                        })}의 축제
                    </h3>
                    {renderSelectedFestivals()}
                </div>
            </div>
            {renderFestivalModal()}
        </div>
    );
};

export default FestivalCalendar;