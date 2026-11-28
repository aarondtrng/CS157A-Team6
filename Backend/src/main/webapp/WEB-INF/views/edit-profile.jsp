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

    <form class="profile-form" method="post" enctype="multipart/form-data"
          action="${pageContext.request.contextPath}/profile/edit?id=${profile.userId}">
      <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />

      <fieldset class="form-section">
        <legend>PROFILE PICTURE</legend>
        <c:choose>
          <c:when test="${profile.profileImage.startsWith('uploads/')}"><c:set var="avatarSrc" value="${pageContext.request.contextPath}/${profile.profileImage}"/></c:when>
          <c:otherwise><c:set var="avatarSrc" value="${profile.profileImage}"/></c:otherwise>
        </c:choose>
        <div class="avatar-edit">
          <span class="avatar-edit-preview" id="avatar-preview" data-initial="<c:out value='${profile.initial}'/>">
            <c:choose>
              <c:when test="${not empty avatarSrc}"><img src="<c:out value='${avatarSrc}'/>" alt="Your current profile picture" /></c:when>
              <c:otherwise><c:out value="${profile.initial}"/></c:otherwise>
            </c:choose>
          </span>
          <div class="field avatar-edit-controls">
            <%-- The real file box is hidden (still focusable and announced); its label is the visible button. --%>
            <input class="visually-hidden" id="profileImage" name="profileImage" type="file" accept="image/jpeg,image/png"
                   aria-describedby="image-hint file-name" />
            <%-- Upload first, then Remove (only when there is a picture to remove), side by side. --%>
            <div class="file-row">
              <label class="file-button" for="profileImage">
                <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 16V4M7 9l5-5 5 5"></path><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3"></path></svg>
                <c:choose>
                  <c:when test="${not empty profile.profileImage}">Upload new picture</c:when>
                  <c:otherwise>Choose a picture</c:otherwise>
                </c:choose>
              </label>
              <c:if test="${not empty profile.profileImage}">
                <%-- A real checkbox, hidden, inside a label styled as a button: clicking anywhere on it toggles the
                     removal (and clicking again undoes it), with or without JavaScript. --%>
                <label class="remove-toggle">
                  <input class="visually-hidden" type="checkbox" id="removeImage" name="removeImage" value="1" />
                  <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14"></path></svg>
                  <span class="remove-toggle-off">Remove current picture</span>
                  <span class="remove-toggle-on">Picture will be removed &middot; Undo</span>
                </label>
              </c:if>
            </div>
            <span class="file-name" id="file-name">No file chosen</span>
            <c:if test="${not empty errors.profileImage}"><div class="flash flash-bad avatar-flash" role="alert"><c:out value="${errors.profileImage}"/></div></c:if>
            <small id="image-hint" class="hint">JPEG or PNG, up to 5 MB. Shown on your profile and your posts.</small>
          </div>
        </div>
      </fieldset>

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
          <input id="email" type="email" readonly tabindex="-1" aria-describedby="email-hint"
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
  <script src="${pageContext.request.contextPath}/js/avatar-picker.js?v=${assetVersion}" defer></script>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
