import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";

const PostWrite = () => {
    const navigate = useNavigate();
    const { postId } = useParams();
    const isEditMode = !!postId;

    const [formData, setFormData] = useState({
        title: "",
        nickname: "",
        mail: "",
        password: "",
        content: "",
        category: "COMPANION",
        tags: "",
    });

    useEffect(() => {
        if (isEditMode) {
            // 수정 모드: 게시글 데이터 불러오기
            const token = localStorage.getItem('accessToken');
            fetch(`/api/companions/${postId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            })
                .then((response) => response.json())
                .then((data) => {
                    setFormData({
                        title: data.title || "",
                        nickname: data.nickname || "",
                        mail: data.mail || "",
                        password: "",
                        content: data.content || "",
                        category: data.category || "COMPANION",
                        tags: (data.tags || []).join(","),
                    });
                })
                .catch((error) => console.error("Error:", error));
        } else {
            // 등록 모드: 사용자 정보 불러오기
            const userInfo = JSON.parse(localStorage.getItem('userInfo'));
            if (userInfo) {
                setFormData((prev) => ({
                    ...prev,
                    nickname: userInfo.nickname || "",
                    mail: userInfo.mail || "",
                }));
            }
        }
    }, [isEditMode, postId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const token = localStorage.getItem("accessToken");

        // 태그 문자열 -> 배열 처리
        const tagsArray = formData.tags
            ? formData.tags.split(",").map((tag) => tag.trim()).filter((tag) => tag !== "")
            : [];

        const processedFormData = isEditMode
            ? {
                // 수정 시
                title: formData.title,
                content: formData.content,
                category: formData.category,
                password: formData.password
            }
            : {
                // 등록 시
                title: formData.title,
                content: formData.content,
                category: formData.category,
                password: formData.password,
                tags: tagsArray
            };

        const url = isEditMode ? `/api/companions/${postId}` : "/api/companions";

        fetch(url, {
            method: isEditMode ? "PUT" : "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(processedFormData),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.text().then(errorData => {
                        console.log('서버 에러 응답:', errorData);
                        throw new Error(isEditMode ? "게시글 수정에 실패했습니다." : "게시글 작성에 실패했습니다.");
                    });
                }
                alert(isEditMode ? "게시글이 수정되었습니다." : "게시글이 등록되었습니다.");
                navigate(isEditMode ? `/post/${postId}` : "/community");
            })
            .catch((error) => {
                console.error("Error:", error);
                alert(error.message || "오류가 발생했습니다.");
            });
    };

    return (
        <div className="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-lg">
            <h1 className="text-2xl font-bold mb-4">{isEditMode ? "게시글 수정" : "새 게시글 작성"}</h1>
            <form onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">제목</label>
                    <input
                        type="text"
                        name="title"
                        value={formData.title}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        required
                    />
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">작성자</label>
                    <input
                        type="text"
                        name="nickname"
                        value={formData.nickname}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        required
                        disabled
                    />
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">이메일</label>
                    <input
                        type="email"
                        name="mail"
                        value={formData.mail}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        required
                        disabled
                    />
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">비밀번호</label>
                    <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        required
                    />
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">내용</label>
                    <textarea
                        name="content"
                        value={formData.content}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        rows="5"
                        required
                    ></textarea>
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">카테고리</label>
                    <select
                        name="category"
                        value={formData.category}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                    >
                        <option value="COMPANION">동행자 모집</option>
                        <option value="REVIEW">후기</option>
                        <option value="QNA">Q&A</option>
                        <option value="OTHER">기타</option>
                    </select>
                </div>
                <div className="mb-4">
                    <label className="block font-semibold mb-2">태그</label>
                    <input
                        type="text"
                        name="tags"
                        value={formData.tags}
                        onChange={handleChange}
                        className="w-full p-2 border rounded-lg"
                        placeholder="태그를 쉼표로 구분해 입력하세요"
                    />
                </div>
                <div className="text-right">
                    <button
                        type="button"
                        onClick={() => navigate("/community")}
                        className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 mr-2"
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className="px-4 py-2 bg-[#4D4B88] text-white rounded-lg hover:opacity-90"
                    >
                        {isEditMode ? "Update" : "Submit"}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default PostWrite;