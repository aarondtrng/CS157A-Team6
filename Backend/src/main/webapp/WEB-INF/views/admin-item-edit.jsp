<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Edit Item | SpartanFinds</title>
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
            <a href="${pageContext.request.contextPath}/admin/users">Users</a>
            <a href="${pageContext.request.contextPath}/admin/reports">Reports</a>
        </nav>
    </div>
</header>

<main class="container">
    <h1>Edit Item</h1>

    <form method="post"
          action="${pageContext.request.contextPath}/admin/items/edit">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${csrfToken}'/>">

        <input type="hidden"
               name="itemId"
               value="${item.itemId}">

        <input type="hidden"
               name="userId"
               value="${item.posterUserId}">

        <label for="title">Title</label>
        <input
            type="text"
            id="title"
            name="title"
            value="<c:out value='${item.title}'/>"
            required>

        <label for="description">Description</label>
        <textarea
            id="description"
            name="description"
            required><c:out value="${item.description}"/></textarea>

        <label for="type">Type</label>
        <select id="type" name="type">
            <option value="LOST" ${item.type == 'LOST' ? 'selected' : ''}>
                Lost
            </option>
            <option value="FOUND" ${item.type == 'FOUND' ? 'selected' : ''}>
                Found
            </option>
        </select>

        <label for="categoryId">Category</label>
        <select id="categoryId" name="categoryId" required>
            <c:forEach var="category" items="${categories}">
                <option
                    value="${category.categoryId}"
                    ${category.categoryId == item.categoryId ? 'selected' : ''}>
                    <c:out value="${category.categoryName}"/>
                </option>
            </c:forEach>
        </select>

        <button type="submit">Save Changes</button>
    </form>
</main>
</body>
</html>
