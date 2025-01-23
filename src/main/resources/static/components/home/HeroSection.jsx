import React from 'react';
import Button from '../common/Button';
import FeatureItem from '../common/FeatureItem';
import { Link } from 'react-router-dom';

const HeroSection = () => {
    const features = [
        '즉시 시작 가능!',
        '당신에게 딱 맞는 동행자와 손쉽게 연결!',
        '간단한 클릭으로 원하는 정보와 연결!',
        '언제 어디서든 편리하게 축제를 준비하세요!',
    ];

    return (
        <div className="container mx-auto px-6 py-12 flex flex-col lg:flex-row items-center">
            {/* Left Content */}
            <div className="lg:w-1/2 lg:pr-12">
                <h1 className="text-4xl font-bold mb-6">
                    축제를 함께할 완벽한 동행자를<br/>
                    <span className="text-[#4D4B88]">만나보세요!</span>
                </h1>
                <p className="text-gray-600 text-lg mb-8">
                    같은 관심사를 가진 사람들과 쉽게 연결되고, 축제를 더욱 특별하게 만들어보세요. 지금 바로 게시글을 확인하고 동행자를 찾아 함께 추억을 만들어보세요.
                </p>
                <div className="space-y-4 mb-8">
                    {features.map((feature, index) => (
                        <FeatureItem key={index} text={feature}/>
                    ))}
                </div>
                <Link to="/community">
                    <Button>동행자 게시판 탐색하기</Button>
                </Link>
            </div>

            {/* Right Image */}
            <div className="lg:w-1/2 mt-12 lg:mt-0 relative">
                <div className="relative">
                    <div className="absolute -top-8 -left-8 w-32 h-32 bg-blue-100 rounded-full opacity-50"/>
                    <img
                        src="/imgs/friend.png"
                        alt="Companion travel"
                        className="rounded-lg shadow-lg relative z-10"
                    />
                    <div className="absolute -bottom-8 -right-8 w-32 h-32 bg-yellow-100 rounded-full opacity-50"/>
                </div>
            </div>
        </div>
    );
};

export default HeroSection;