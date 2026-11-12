<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin User Search | SpartanFinds</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/catalog.css">
</head>

<body>
<header class="topbar">
    <div class="container topbar-inner">
        <a class="brand"
           href="${pageContext.request.contextPath}/items">
            SpartanFinds
        </a>

        <nav>
            <a href="${pageContext.request.contextPath}/items">Browse</a>
            <a href="${pageContext.request.contextPath}/admin/reports">Reports</a>
        </nav>
    </div>
</header>

<main class="container">
    <h1>Search Users</h1>
    <p>Administrators can search registered users by name or ID.</p>

    <form method="get"
          action="${pageContext.request.contextPath}/admin/users">

        <label for="name">User name</label>
        <input
            type="text"
            id="name"
            name="name"
            value="<c:out value='${searchName}'/>"
            placeholder="Enter a name">

        <label for="id">User ID</label>
        <input
            type="number"
            id="id"
            name="id"
            value="<c:out value='${searchId}'/>"
            placeholder="Enter an ID">

        <button type="submit">Search</button>
    </form>

    <c:if test="${not empty searchName or not empty searchId}">
        <h2>Search Results</h2>

        <c:choose>
            <c:when test="${empty profiles}">
                <p>No users were found.</p>
            </c:when>

            <c:otherwise>
                <c:forEach var="profile" items="${profiles}">
                    <section>
                        <h3>
                            <c:out value="${profile.name}"/>
                        </h3>

                        <p>
                            User ID:
                            <c:out value="${profile.userId}"/>
                        </p>

                        <p>
                            Email:
                            <c:out value="${profile.email}"/>
                        </p>

                        <p>
                            Role:
                            <c:out value="${profile.role}"/>
                        </p>

                        <a href="${pageContext.request.contextPath}/profile?id=${profile.userId}">
                            View Profile
                        </a>
                    </section>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>
    <c:if test="${not empty selectedUserId and not empty profiles}">
    <form method="post"
          action="${pageContext.request.contextPath}/admin/users">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${csrfToken}'/>">

        <input type="hidden"
               name="action"
               value="deleteProfile">

        <input type="hidden"
               name="userId"
               value="${selectedUserId}">

        <button type="submit">Delete Profile</button>
    </form>
    </c:if>
    <c:if test="${not empty selectedUserId}">
    <h2>Item Posts</h2>

    <c:choose>
        <c:when test="${empty items}">
            <p>This user has no public item posts.</p>
        </c:when>

        <c:otherwise>
            <c:forEach var="item" items="${items}">
                <section>
                    <h3>
                        <c:out value="${item.title}"/>
                    </h3>

                    <p>
                        Item ID:
                        <c:out value="${item.itemId}"/>
                    </p>

                    <p>
                        Type:
                        <c:out value="${item.type}"/>
                    </p>

                    <p>
                        Category:
                        <c:out value="${item.categoryName}"/>
                    </p>

                    <p>
                        Status:
                        <c:out value="${item.status}"/>
                    </p>

                    <a href="${pageContext.request.contextPath}/item?id=${item.itemId}">
                        View Item
                    </a>

                    <a href="${pageContext.request.contextPath}/admin/items/edit?itemId=${item.itemId}">
                        Edit Item
                    </a>
                    <form method="post"
      action="${pageContext.request.contextPath}/admin/users">

    <input type="hidden"
           name="csrfToken"
           value="<c:out value='${csrfToken}'/>">

    <input type="hidden"
           name="itemId"
           value="${item.itemId}">

    <input type="hidden"
           name="userId"
           value="${selectedUserId}">

    <button type="submit">Remove Item</button>
</form>
                </section>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>
</main>
</body>
</html>