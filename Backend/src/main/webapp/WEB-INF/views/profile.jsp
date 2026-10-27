<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: UserProfileServlet supplies profile, loggedIn, isOwner. Every value is escaped with c:out,
     because names and descriptions are typed by users. Email and phone are shown only to logged-in students. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title><c:out value="${profile.name}"/> | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css?v=${assetVersion}" />
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
    <c:set var="ctx" value="${pageContext.request.contextPath}"/>
    <section class="profile-card panel" aria-labelledby="profile-name">
      <div class="profile-banner" aria-hidden="true"><span class="profile-ring"></span></div>
      <div class="profile-head">
        <span class="profile-avatar" aria-hidden="true"><c:out value="${profile.initial}"/></span>
        <div class="profile-title">
          <h1 id="profile-name"><c:out value="${profile.name}"/></h1>
          <p>Student</p>
        </div>
        <c:if test="${isOwner}">
          <a class="btn" href="${ctx}/profile/edit?id=${profile.userId}">Edit profile</a>
        </c:if>
      </div>
      <%-- All on one line: the description keeps its line breaks (pre-line), so no stray whitespace inside. --%>
      <p class="profile-bio"><c:choose><c:when test="${empty profile.profileDescription}">No description provided.</c:when><c:otherwise><c:out value="${profile.profileDescription}"/></c:otherwise></c:choose></p>
    </section>

    <aside class="contact-card panel" aria-label="Contact">
      <h2>CONTACT</h2>
      <c:choose>
        <c:when test="${loggedIn}">
          <dl>
            <div>
              <dt>SJSU email</dt>
              <dd><c:out value="${profile.email}"/></dd>
            </div>
            <div>
              <dt>Phone</dt>
              <dd>
                <c:choose>
                  <c:when test="${empty profile.phoneNumber}">Not provided</c:when>
                  <c:otherwise><c:out value="${profile.phoneNumber}"/></c:otherwise>
                </c:choose>
              </dd>
            </div>
          </dl>
        </c:when>
        <c:otherwise>
          <p class="contact-note">Log in to see this student&rsquo;s contact details.</p>
        </c:otherwise>
      </c:choose>
    </aside>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
