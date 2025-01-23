import React from 'react';

const Button = ({ children, variant = 'primary', className = '', ...props }) => {
    const baseStyles = 'px-6 py-3 rounded-lg font-medium transition-colors duration-200';

    const variants = {
        primary: 'bg-gray-900 text-white hover:bg-gray-800',
        secondary: 'bg-white text-gray-900 border border-gray-200 hover:bg-gray-50',
    };

    return (
        <button
            className={`${baseStyles} ${variants[variant]} ${className}`}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;