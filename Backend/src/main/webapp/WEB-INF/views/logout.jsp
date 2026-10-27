<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: LogoutServlet supplies csrfToken. Asks before ending the session, because the landing page
     links here and cannot send the POST itself. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Log Out | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=${assetVersion}" />
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
    </div>
  </header>

  <main class="container">
    <section class="auth-card" aria-labelledby="logout-heading">
      <h1 id="logout-heading">Log out?</h1>
      <p class="subtitle">You are logged in as <strong><c:out value="${sessionScope.userName}"/></strong>.</p>
      <form class="auth-form" method="post" action="${pageContext.request.contextPath}/logout">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
        <button type="submit" class="btn btn-block">Log out</button>
      </form>
      <p class="auth-switch"><a href="${pageContext.request.contextPath}/">Cancel and go back</a></p>
    </section>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
