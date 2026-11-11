<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Access denied | SpartanFinds</title>
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
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
    <h1>Access denied</h1>
    <c:choose>
      <c:when test="${fn:contains(requestScope['jakarta.servlet.error.request_uri'], '/admin/')}">
        <p>Only administrator accounts can open this page.</p>
        <p>If your account was recently made an administrator, log out using the account menu and log in again.</p>
      </c:when>
      <c:otherwise>
        <p>You do not have permission to open this page or perform this action.</p>
        <p>If you submitted a form, reopen its page and try again.</p>
      </c:otherwise>
    </c:choose>
    <p><a class="btn" href="${pageContext.request.contextPath}/items">Browse items</a></p>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
