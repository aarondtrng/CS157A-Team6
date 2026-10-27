<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: LoginServlet supplies next, and after a failed POST email, error, or notImplemented. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Log In | SpartanFinds</title>
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
    <section class="auth-card" aria-labelledby="login-heading">
      <h1 id="login-heading">Log in</h1>
      <p class="subtitle">Use your SJSU email to continue.</p>

      <c:if test="${notImplemented}">
        <div class="notice" role="status">
          <strong>Login is not available yet.</strong>
          The database query for accounts has not been implemented (see <code>UserDao.findByEmail</code>).
        </div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="form-error" role="alert"><c:out value="${error}"/></div>
      </c:if>

      <noscript><div class="form-error">JavaScript is required to log in (your password is hashed in the browser).</div></noscript>
      <div class="form-error" data-client-error hidden role="alert"></div>

      <%-- The password input has no name, so it is never submitted; js/auth.js puts its hash in "password". --%>
      <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login" data-hash-password>
        <input type="hidden" name="next" value="<c:out value='${next}'/>" />
        <input type="hidden" name="password" />
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
        <div class="field">
          <label for="email">SJSU email</label>
          <input id="email" name="email" type="email" required maxlength="255" autocomplete="username"
                 placeholder="first.last@sjsu.edu" value="<c:out value='${email}'/>" />
        </div>
        <div class="field">
          <label for="password">Password</label>
          <input id="password" type="password" required maxlength="128" autocomplete="current-password"
                 data-password />
        </div>
        <button type="submit" class="btn btn-block">Log in</button>
      </form>

      <c:url var="signupUrl" value="/signup"><c:param name="next" value="${next}"/></c:url>
      <p class="auth-switch">New to SpartanFinds? <a href="${signupUrl}">Create an account</a></p>
    </section>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
  <script src="${pageContext.request.contextPath}/js/auth.js?v=${assetVersion}"></script>
</body>
</html>
