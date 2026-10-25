<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: CatalogServlet supplies query, sortOptions, categories, catalogPage, notImplemented. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Browse Items | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css" />
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="<c:out value='${initParam.frontendUrl}'/>">SpartanFinds</a>
      <%-- TODO: nav links and login/profile controls once those pages exist. --%>
    </div>
  </header>

  <main class="container">
    <h1>Browse Items</h1>
    <p class="subtitle">Search lost and found items reported around campus.</p>

    <form class="filters" method="get" action="${pageContext.request.contextPath}/items">
      <div class="field field-search">
        <label for="q">Search</label>
        <input id="q" name="q" type="search" maxlength="100" placeholder="Title or description"
               value="<c:out value='${query.searchText}'/>" />
      </div>

      <div class="field">
        <label for="category">Category</label>
        <select id="category" name="category">
          <option value="">All categories</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.categoryId}" ${cat.categoryId == query.categoryId ? 'selected' : ''}>
              <c:out value="${cat.categoryName}"/>
            </option>
          </c:forEach>
        </select>
      </div>

      <div class="field">
        <label for="sort">Sort by</label>
        <select id="sort" name="sort">
          <c:forEach var="option" items="${sortOptions}">
            <option value="${option.param}" ${option == query.sort ? 'selected' : ''}>
              <c:out value="${option.label}"/>
            </option>
          </c:forEach>
        </select>
      </div>

      <button type="submit" class="btn">Apply</button>
    </form>

    <c:choose>
      <c:when test="${notImplemented}">
        <div class="notice" role="status">
          <strong>Catalog data is not available yet.</strong>
          The database queries for browsing items have not been implemented
          (see <code>ItemDao</code>).
        </div>
      </c:when>

      <c:when test="${empty catalogPage.items}">
        <div class="notice" role="status">No items match your search.</div>
      </c:when>

      <c:otherwise>
        <p class="result-count" role="status">
          ${catalogPage.totalCount} item${catalogPage.totalCount == 1 ? '' : 's'} found
        </p>

        <%-- Cards link to the post page; guests are redirected to login there. --%>
        <ul class="grid">
          <c:forEach var="item" items="${catalogPage.items}">
            <c:url var="postUrl" value="/item">
              <c:param name="id" value="${item.itemId}"/>
            </c:url>
            <li>
              <article class="card">
                <div class="card-image">
                  <c:choose>
                    <c:when test="${not empty item.thumbnailUrl}">
                      <img src="<c:out value='${item.thumbnailUrl}'/>" alt="<c:out value='${item.title}'/>" loading="lazy" />
                    </c:when>
                    <c:otherwise>
                      <span class="no-image">No image</span>
                    </c:otherwise>
                  </c:choose>
                  <span class="badge badge-${item.type == 'FOUND' ? 'found' : 'lost'}">
                    <c:out value="${item.type}"/>
                  </span>
                </div>
                <div class="card-body">
                  <h2><a class="card-link" href="${postUrl}"><c:out value="${item.title}"/></a></h2>
                  <p><c:out value="${item.briefDescription}"/></p>
                  <small>
                    <c:out value="${item.categoryName}"/> &middot; <c:out value="${item.createdDateText}"/>
                  </small>
                </div>
              </article>
            </li>
          </c:forEach>
        </ul>

        <nav class="pager" aria-label="Catalog pages">
          <c:if test="${catalogPage.hasPrevious}">
            <c:url var="prevUrl" value="/items">
              <c:param name="q" value="${query.searchText}"/>
              <c:param name="category" value="${query.categoryId}"/>
              <c:param name="sort" value="${query.sort.param}"/>
              <c:param name="page" value="${catalogPage.page - 1}"/>
            </c:url>
            <a class="btn btn-outline" href="${prevUrl}">Previous</a>
          </c:if>
          <span>Page ${catalogPage.page} of ${catalogPage.totalPages}</span>
          <c:if test="${catalogPage.hasNext}">
            <c:url var="nextUrl" value="/items">
              <c:param name="q" value="${query.searchText}"/>
              <c:param name="category" value="${query.categoryId}"/>
              <c:param name="sort" value="${query.sort.param}"/>
              <c:param name="page" value="${catalogPage.page + 1}"/>
            </c:url>
            <a class="btn btn-outline" href="${nextUrl}">Next</a>
          </c:if>
        </nav>
      </c:otherwise>
    </c:choose>
  </main>
</body>
</html>
