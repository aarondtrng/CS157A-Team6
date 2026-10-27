<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: RequestFormServlet supplies post (ItemDetail), blockedReason (null when allowed), description, errors, csrfToken, minLength, maxLength. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Request this item | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/requests.css?v=${assetVersion}" />
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
      <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
    </div>
  </header>

  <main class="container">
    <a class="back-link" href="${pageContext.request.contextPath}/item?id=${post.itemId}">&larr; Back to the post</a>
    <h1>Request this item</h1>
    <p class="subtitle">
      You are asking for <strong><c:out value="${post.title}"/></strong>. The student who found it will read
      your answer and decide. Nobody else can see it.
    </p>

    <c:choose>
      <c:when test="${not empty blockedReason}">
        <div class="notice" role="status"><c:out value="${blockedReason}"/></div>
        <p class="request-links">
          <a class="btn btn-outline" href="${pageContext.request.contextPath}/requests">My requests</a>
        </p>
      </c:when>

      <c:otherwise>
        <c:if test="${not empty errors}">
          <div class="form-error" role="alert">
            <c:choose>
              <c:when test="${not empty errors.form}"><c:out value="${errors.form}"/></c:when>
              <c:otherwise>Please fix the problem below and send your request again.</c:otherwise>
            </c:choose>
          </div>
        </c:if>

        <form class="post-form" method="post" action="${pageContext.request.contextPath}/request/new">
          <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
          <input type="hidden" name="itemId" value="${post.itemId}" />

          <div class="field">
            <label for="description">How can you prove it is yours?</label>
            <textarea id="description" name="description" rows="8" required
                      minlength="${minLength}" maxlength="${maxLength}"
                      aria-describedby="description-hint"><c:out value="${description}"/></textarea>
            <small id="description-hint" class="hint">
              Give details only the owner would know, such as marks, contents, a lock screen, or when and
              where you lost it. Do not include passwords or ID numbers.
            </small>
            <c:if test="${not empty errors.description}"><p class="field-error" role="alert"><c:out value="${errors.description}"/></p></c:if>
          </div>

          <button type="submit" class="btn">Send request</button>
        </form>
      </c:otherwise>
    </c:choose>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
