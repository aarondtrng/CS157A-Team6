<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: PostItemServlet supplies form (PostForm), errors (field -> message), categories, csrfToken, maxImages. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Post an Item | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/post-map.js?v=${assetVersion}" defer></script>
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="<c:out value='${initParam.frontendUrl}'/>">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
      <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
    </div>
  </header>

  <main class="container">
    <a class="back-link" href="${pageContext.request.contextPath}/items">&larr; Back to all items</a>
    <h1>Post an item</h1>
    <p class="subtitle">Tell other students what you lost or found. Only your name is shown, never your email.</p>

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

      <fieldset class="field">
        <legend>This item was</legend>
        <label class="choice"><input type="radio" name="type" value="LOST" required
                                     ${form.type == 'LOST' ? 'checked' : ''} /> Lost</label>
        <label class="choice"><input type="radio" name="type" value="FOUND"
                                     ${form.type == 'FOUND' ? 'checked' : ''} /> Found</label>
        <c:if test="${not empty errors.type}"><p class="field-error" role="alert"><c:out value="${errors.type}"/></p></c:if>
      </fieldset>

      <div class="field">
        <label for="title">Title</label>
        <input id="title" name="title" type="text" required maxlength="255"
               placeholder="e.g. Blue Hydro Flask" value="<c:out value='${form.title}'/>" />
        <c:if test="${not empty errors.title}"><p class="field-error" role="alert"><c:out value="${errors.title}"/></p></c:if>
      </div>

      <div class="field">
        <label for="categoryId">Category</label>
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
        <label for="description">Description</label>
        <textarea id="description" name="description" rows="6" required maxlength="5000"
                  placeholder="Color, brand, where exactly, anything that helps. Do not post private details such as ID numbers."><c:out value="${form.description}"/></textarea>
        <c:if test="${not empty errors.description}"><p class="field-error" role="alert"><c:out value="${errors.description}"/></p></c:if>
      </div>

      <div class="field">
        <label for="images">Photos (optional)</label>
        <input id="images" name="images" type="file" multiple accept="image/jpeg,image/png"
               aria-describedby="images-hint" />
        <small id="images-hint" class="hint">Up to ${maxImages} JPEG or PNG photos, 5 MB each. Photos are not kept if the form has an error, so choose them again.</small>
        <c:if test="${not empty errors.images}"><p class="field-error" role="alert"><c:out value="${errors.images}"/></p></c:if>
      </div>

      <fieldset class="field">
        <legend>Where was it lost or found?</legend>
        <p class="hint" id="map-hint">Click the map to drop a pin on the exact spot. Others will see it there.</p>
        <noscript><p class="field-error">Choosing a location needs JavaScript. Please turn it on to post.</p></noscript>
        <div id="location-map" class="map" role="application" aria-describedby="map-hint"
             aria-label="Campus map. Click to choose the location."></div>
        <input type="hidden" id="latitude" name="latitude" value="<c:out value='${form.latitude}'/>" />
        <input type="hidden" id="longitude" name="longitude" value="<c:out value='${form.longitude}'/>" />
        <p class="map-status"><span id="location-chosen">No spot chosen yet.</span>
          <button type="button" id="location-clear" class="btn btn-outline btn-small" hidden>Clear pin</button></p>
        <label for="locationName">Place name (optional)</label>
        <input id="locationName" name="locationName" type="text" maxlength="255"
               placeholder="e.g. Near the library entrance" value="<c:out value='${form.locationName}'/>" />
        <c:if test="${not empty errors.location}"><p class="field-error" role="alert"><c:out value="${errors.location}"/></p></c:if>
      </fieldset>

      <button type="submit" class="btn">Post item</button>
    </form>
  </main>
</body>
</html>
