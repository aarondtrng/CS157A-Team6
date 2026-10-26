<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>${profile.name}'s Profile</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f8;
            margin: 0;
            padding: 40px;
        }

        .profile-card {
            max-width: 600px;
            margin: auto;
            padding: 30px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
        }

        .profile-avatar {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: #1261a0;
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 36px;
            margin-bottom: 20px;
        }

        h1 {
            color: #12355b;
        }

        .profile-field {
            margin: 14px 0;
        }

        .profile-field strong {
            display: inline-block;
            width: 160px;
        }
    </style>
</head>

<body>
    <main class="profile-card">
        <div class="profile-avatar">
            ${profile.name.substring(0, 1)}
        </div>

        <h1>${profile.name}'s Profile</h1>

        <div class="profile-field">
            <strong>Email:</strong>
            ${profile.email}
        </div>

        <div class="profile-field">
            <strong>Phone:</strong>
            ${empty profile.phoneNumber ? "Not provided" : profile.phoneNumber}
        </div>

        <div class="profile-field">
            <strong>Description:</strong>
            ${empty profile.profileDescription
                ? "No description provided."
                : profile.profileDescription}
        </div>

        <div class="profile-field">
            <strong>Account role:</strong>
            ${profile.role}
        </div>

        <p>
            <a href="${pageContext.request.contextPath}/profile/edit?id=${profile.userId}">
                Edit Profile
            </a>
        </p>
    </main>
</body>
</html>