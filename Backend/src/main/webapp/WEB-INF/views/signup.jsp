<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: SignupServlet supplies next, and after a failed POST firstName, lastName, email, errors, or notImplemented. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Sign Up | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css?v=${assetVersion}" />
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="<c:out value='${initParam.frontendUrl}'/>">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
    </div>
  </header>

  <main class="container">
    <section class="auth-card" aria-labelledby="signup-heading">
      <h1 id="signup-heading">Create an account</h1>
      <p class="subtitle">Sign up with your SJSU email address.</p>

      <c:if test="${notImplemented}">
        <div class="notice" role="status">
          <strong>Sign up is not available yet.</strong>
          The database query for creating accounts has not been implemented (see <code>UserDao.insertStudent</code>).
        </div>
      </c:if>
      <c:if test="${not empty errors}">
        <div class="form-error" role="alert">
          <ul>
            <c:forEach var="message" items="${errors}">
              <li><c:out value="${message}"/></li>
            </c:forEach>
          </ul>
        </div>
      </c:if>

      <noscript><div class="form-error">JavaScript is required to sign up (your password is hashed in the browser).</div></noscript>
      <div class="form-error" data-client-error hidden role="alert"></div>

      <%-- Password inputs have no name, so they are never submitted; js/auth.js puts the hash in "password". --%>
      <form class="auth-form" method="post" action="${pageContext.request.contextPath}/signup" data-hash-password>
        <input type="hidden" name="next" value="<c:out value='${next}'/>" />
        <input type="hidden" name="password" />
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
        <div class="field">
          <label for="email">SJSU email</label>
          <input id="email" name="email" type="email" required maxlength="255" autocomplete="username"
                 pattern=".+@sjsu\.edu" title="Must end in @sjsu.edu"
                 placeholder="first.last@sjsu.edu" value="<c:out value='${email}'/>" />
        </div>
        <div class="field-row">
          <div class="field">
            <label for="firstName">First name</label>
            <input id="firstName" name="firstName" type="text" required maxlength="49" autocomplete="given-name"
                   value="<c:out value='${firstName}'/>" />
          </div>
          <div class="field">
            <label for="lastName">Last name</label>
            <input id="lastName" name="lastName" type="text" required maxlength="49" autocomplete="family-name"
                   value="<c:out value='${lastName}'/>" />
          </div>
        </div>
        <div class="field">
          <label for="password">Password</label>
          <input id="password" type="password" required minlength="8" maxlength="128"
                 autocomplete="new-password" aria-describedby="password-hint" data-password />
          <small id="password-hint" class="hint">At least 8 characters.</small>
        </div>
        <div class="field">
          <label for="confirmPassword">Confirm password</label>
          <input id="confirmPassword" type="password" required minlength="8" maxlength="128"
                 autocomplete="new-password" data-password-confirm />
        </div>
        <button type="submit" class="btn btn-block">Sign up</button>
      </form>

      <c:url var="loginUrl" value="/login"><c:param name="next" value="${next}"/></c:url>
      <p class="auth-switch">Already have an account? <a href="${loginUrl}">Log in</a></p>
    </section>
  </main>
  <script src="${pageContext.request.contextPath}/js/auth.js?v=${assetVersion}"></script>
</body>
</html>
