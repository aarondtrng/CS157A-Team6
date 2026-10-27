<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: EditProfileServlet supplies profile, name, phoneNumber, profileDescription, errors, csrfToken.
     Every value is escaped with c:out, including the ones put back into the fields after an error. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Edit Profile | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css?v=${assetVersion}" />
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

  <main class="container narrow">
    <a class="back-link" href="${pageContext.request.contextPath}/profile?id=${profile.userId}">&larr; Back to profile</a>
    <h1 id="edit-heading">Edit profile</h1>
    <p class="subtitle">Your name and description are visible to other students.</p>

    <c:if test="${not empty errors}">
      <div class="form-error" role="alert">
        <c:choose>
          <c:when test="${not empty errors.form}"><c:out value="${errors.form}"/></c:when>
          <c:otherwise>Please fix the problems below and save again.</c:otherwise>
        </c:choose>
      </div>
    </c:if>

    <form class="profile-form" method="post"
          action="${pageContext.request.contextPath}/profile/edit?id=${profile.userId}">
      <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />

      <fieldset class="form-section">
        <legend>ABOUT YOU</legend>
        <div class="field">
          <label for="name">Full name *</label>
          <input id="name" name="name" type="text" required maxlength="100" autocomplete="name"
                 value="<c:out value='${name}'/>" />
          <c:if test="${not empty errors.name}"><p class="field-error" role="alert"><c:out value="${errors.name}"/></p></c:if>
        </div>
        <div class="field">
          <label for="profileDescription">Short description <span class="optional">(optional)</span></label>
          <textarea id="profileDescription" name="profileDescription" rows="4" maxlength="1000"
                    placeholder="Your major, where you usually are on campus, the best time to reach you&hellip;"><c:out value="${profileDescription}"/></textarea>
          <c:if test="${not empty errors.profileDescription}"><p class="field-error" role="alert"><c:out value="${errors.profileDescription}"/></p></c:if>
        </div>
      </fieldset>

      <fieldset class="form-section">
        <legend>CONTACT</legend>
        <div class="field">
          <label for="email">SJSU email</label>
          <input id="email" type="email" readonly aria-describedby="email-hint"
                 value="<c:out value='${profile.email}'/>" />
          <small id="email-hint" class="hint">Your sign-in email can&rsquo;t be changed here.</small>
        </div>
        <div class="field">
          <label for="phoneNumber">Phone number <span class="optional">(optional)</span></label>
          <input id="phoneNumber" name="phoneNumber" type="tel" inputmode="tel" maxlength="20" autocomplete="tel"
                 pattern="\(\d{3}\) \d{3}-\d{4}" title="A 10-digit phone number, like (408) 555-0123"
                 aria-describedby="phone-hint" placeholder="(408) 555-0123" value="<c:out value='${phoneNumber}'/>" />
          <small id="phone-hint" class="hint">10 digits, like (408) 555-0123. Shown on your profile to signed-in students.</small>
          <c:if test="${not empty errors.phoneNumber}"><p class="field-error" role="alert"><c:out value="${errors.phoneNumber}"/></p></c:if>
        </div>
      </fieldset>

      <div class="form-actions">
        <a class="btn btn-outline btn-lg" href="${pageContext.request.contextPath}/profile?id=${profile.userId}">Cancel</a>
        <button type="submit" class="btn btn-lg">Save changes</button>
      </div>
    </form>
  </main>
  <script src="${pageContext.request.contextPath}/js/phone-input.js?v=${assetVersion}" defer></script>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
