import {useParams} from "react-router-dom";

const Profile = () => {
    const { userId } = useParams();

    return (
        <div className="max-w-4xl mx-auto p-6">
            <h1 className="text-2xl font-bold">사용자 프로필 (임시)</h1> // TODO: Profile 컴포넌트
            <p>User ID: {userId}</p>
        </div>
    );
};

export default Profile;