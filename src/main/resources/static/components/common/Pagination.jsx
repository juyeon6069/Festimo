import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({
                        currentPage,
                        totalPages,
                        onPageChange
                    }) => {
    // 표시할 페이지 번호 계산
    const getPageNumbers = () => {
        const delta = 2; // 현재 페이지 좌우로 보여줄 페이지 수
        const range = [];
        const rangeWithDots = [];

        for (
            let i = Math.max(2, currentPage - delta);
            i <= Math.min(totalPages - 1, currentPage + delta);
            i++
        ) {
            range.push(i);
        }

        if (currentPage - delta > 2) {
            rangeWithDots.push(1, '...');
        } else {
            rangeWithDots.push(1);
        }

        rangeWithDots.push(...range);

        if (currentPage + delta < totalPages - 1) {
            rangeWithDots.push('...', totalPages);
        } else if (totalPages > 1) {
            rangeWithDots.push(totalPages);
        }

        return rangeWithDots;
    };

    const handlePageClick = (page) => {
        if (page !== '...' && page !== currentPage) {
            onPageChange(page);
        }
    };

    if (totalPages <= 1) return null;

    return (
        <div className="flex items-center justify-center gap-1 mt-8 mb-4">
            {/* 이전 페이지 버튼 */}
            <button
                onClick={() => currentPage > 1 && handlePageClick(currentPage - 1)}
                disabled={currentPage === 1}
                className={`p-2 rounded-lg hover:bg-gray-100 transition-colors
                    ${currentPage === 1 ? 'text-gray-300 cursor-not-allowed' : 'text-gray-600'}`}
            >
                <ChevronLeft className="w-5 h-5" />
            </button>

            {/* 페이지 번호들 */}
            {getPageNumbers().map((pageNumber, index) => (
                <button
                    key={index}
                    onClick={() => handlePageClick(pageNumber)}
                    className={`px-4 py-2 rounded-lg transition-colors
            ${pageNumber === currentPage
                        ? 'bg-custom-purple text-white'  // 활성 페이지 스타일
                        : pageNumber === '...'
                            ? 'cursor-default'
                            : 'hover:bg-gray-100 text-custom-purple'  // 비활성 페이지 스타일
                    }`}
                >
                    {pageNumber}
                </button>
            ))}

            {/* 다음 페이지 버튼 */}
            <button
                onClick={() => currentPage < totalPages && handlePageClick(currentPage + 1)}
                disabled={currentPage === totalPages}
                className={`p-2 rounded-lg hover:bg-gray-100 transition-colors
                    ${currentPage === totalPages ? 'text-gray-300 cursor-not-allowed' : 'text-gray-600'}`}
            >
                <ChevronRight className="w-5 h-5" />
            </button>
        </div>
    );
};

export default Pagination;