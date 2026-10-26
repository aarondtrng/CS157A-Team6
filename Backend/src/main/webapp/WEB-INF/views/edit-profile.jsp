<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Edit Profile</title>
</head>

<body>
    <main>
        <h1>Edit Profile</h1>

        <form method="post"
              action="${pageContext.request.contextPath}/profile/edit?id=${profile.userId}">

            <label>
                Name:
                <input type="text"
                       name="name"
                       value="${profile.name}"
                       required>
            </label>

            <br><br>

            <label>
                Email:
                <input type="email"
                       value="${profile.email}"
                       readonly>
            </label>

            <br><br>

            <label>
                Phone:
                <input type="text"
                       name="phoneNumber"
                       value="${profile.phoneNumber}">
            </label>

            <br><br>

            <label>
                Profile description:
                <br>
                <textarea name="profileDescription"
                          rows="5"
                          cols="40">${profile.profileDescription}</textarea>
            </label>

            <br><br>

            <label>
                Profile image URL:
                <input type="text"
                       name="profileImage"
                       value="${profile.profileImage}">
            </label>

            <br><br>

            <button type="submit">Save Changes</button>
        </form>

        <p>
            <a href="${pageContext.request.contextPath}/profile?id=${profile.userId}">
                Cancel
            </a>
        </p>
    </main>
</body>
</html>