<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: PostItemServlet supplies form (PostForm), errors (field -> message), categories, csrfToken, maxImages. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Post an Item | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/photo-picker.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/map-pin.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/post-map.js?v=${assetVersion}" defer></script>
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
    <a class="back-link" href="${pageContext.request.contextPath}/items">&larr; Back to items</a>
    <h1 class="title-spaced">New post</h1>

    <c:if test="${not empty errors}">
      <div class="form-error" role="alert">
        <c:choose>
          <c:when test="${not empty errors.form}"><c:out value="${errors.form}"/></c:when>
          <c:otherwise>Please fix the problems below and submit again.</c:otherwise>
        </c:choose>
      </div>
    </c:if>

    <form class="post-form" method="post" enctype="multipart/form-data"
          action="${pageContext.request.contextPath}/post/new">
      <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />

      <fieldset class="form-section">
        <legend>WHAT HAPPENED</legend>
        <div class="type-grid" role="radiogroup" aria-label="Post type">
          <label class="type-card">
            <input type="radio" name="type" value="LOST" required ${form.type == 'LOST' ? 'checked' : ''} />
            <span><strong>I lost something</strong><small>Ask others to keep an eye out</small></span>
          </label>
          <label class="type-card">
            <input type="radio" name="type" value="FOUND" ${form.type == 'FOUND' ? 'checked' : ''} />
            <span><strong>I found something</strong><small>Help it get back to its owner</small></span>
          </label>
        </div>
        <c:if test="${not empty errors.type}"><p class="field-error" role="alert"><c:out value="${errors.type}"/></p></c:if>

        <div class="field">
          <label for="title">Title *</label>
          <input id="title" name="title" type="text" required maxlength="255"
                 placeholder="e.g. Blue Hydro Flask" value="<c:out value='${form.title}'/>" />
          <c:if test="${not empty errors.title}"><p class="field-error" role="alert"><c:out value="${errors.title}"/></p></c:if>
        </div>

        <div class="field">
          <label for="categoryId">Category *</label>
          <select id="categoryId" name="categoryId" required>
            <option value="">Choose a category</option>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.categoryId}" ${cat.categoryId == form.categoryId ? 'selected' : ''}>
                <c:out value="${cat.categoryName}"/>
              </option>
            </c:forEach>
          </select>
          <c:if test="${not empty errors.category}"><p class="field-error" role="alert"><c:out value="${errors.category}"/></p></c:if>
        </div>

        <div class="field">
          <label for="description">Description *</label>
          <textarea id="description" name="description" rows="4" required maxlength="5000"
                    aria-describedby="description-hint"><c:out value="${form.description}"/></textarea>
          <span id="description-hint" class="hint">For found items, leave out identifying details so the owner can prove it&rsquo;s theirs. Do not post private details such as ID numbers.</span>
          <c:if test="${not empty errors.description}"><p class="field-error" role="alert"><c:out value="${errors.description}"/></p></c:if>
        </div>
      </fieldset>

      <fieldset class="form-section">
        <legend>PHOTOS</legend>
        <label class="dropzone" for="images">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 16V4M7 9l5-5 5 5"></path><path d="M4 16v3a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-3"></path></svg>
          <span class="dropzone-title">Choose photos</span>
          <span id="images-hint" class="hint">Up to ${maxImages} JPEG or PNG images, 5 MB each. You can choose more than one at a time, and choosing again adds to your selection. The first photo is the cover. Photos are not kept if the form has an error, so choose them again.</span>
          <input id="images" name="images" type="file" multiple accept="image/jpeg,image/png"
                 data-max="${maxImages}" aria-describedby="images-hint" />
        </label>
        <p id="photo-note" class="field-error" role="alert" hidden></p>
        <ul id="photo-previews" class="photo-previews" aria-label="Chosen photos"></ul>
        <c:if test="${not empty errors.images}"><p class="field-error" role="alert"><c:out value="${errors.images}"/></p></c:if>
      </fieldset>

      <fieldset class="form-section">
        <legend>LOCATION *</legend>
        <p class="hint" id="map-hint">Click the map where the item was lost or found. Others will see the pin there.</p>
        <noscript><p class="field-error">Choosing a location needs JavaScript. Please turn it on to post.</p></noscript>
        <div id="location-map" class="map" role="application" aria-describedby="map-hint"
             aria-label="Campus map. Click to choose the location."></div>
        <input type="hidden" id="latitude" name="latitude" value="<c:out value='${form.latitude}'/>" />
        <input type="hidden" id="longitude" name="longitude" value="<c:out value='${form.longitude}'/>" />
        <div class="map-status">
          <span class="spot-pill"><span id="location-chosen">No spot chosen yet.</span></span>
          <button type="button" id="location-clear" class="btn btn-outline btn-small" hidden>Clear pin</button>
        </div>
        <div class="field">
          <label for="locationName">Place name</label>
          <input id="locationName" name="locationName" type="text" maxlength="255"
                 placeholder="e.g. Near the library entrance" value="<c:out value='${form.locationName}'/>" />
        </div>
        <c:if test="${not empty errors.location}"><p class="field-error" role="alert"><c:out value="${errors.location}"/></p></c:if>
      </fieldset>

      <div class="form-actions">
        <a class="btn btn-outline btn-lg" href="${pageContext.request.contextPath}/items">Cancel</a>
        <button type="submit" class="btn btn-lg">Publish post</button>
      </div>
    </form>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
