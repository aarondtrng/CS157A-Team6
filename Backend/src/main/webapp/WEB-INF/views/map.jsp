<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: MapServlet supplies mapItems. The item list below is real page content (it also works
     without JavaScript); js/items-map.js reads its data-* attributes to place the map markers. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Campus Map | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/map.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/items-map.js?v=${assetVersion}" defer></script>
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="<c:out value='${initParam.frontendUrl}'/>">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
      <c:set var="hideMapLink" value="true"/>
      <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
    </div>
  </header>

  <main class="container">
    <a class="back-link" href="${pageContext.request.contextPath}/items">&larr; Back to all items</a>
    <h1>Campus map</h1>
    <p class="subtitle">Where items were lost or found. Each pin marks the spot where the poster lost or found the item.</p>

    <div id="items-map" class="map map-large" role="application"
         aria-label="Campus map with pins for lost and found items. The same items are listed below the map."></div>

    <h2 class="map-list-heading">Items on the map</h2>
    <c:choose>
      <c:when test="${empty mapItems}">
        <div class="notice" role="status">No items with a location have been posted yet.</div>
      </c:when>
      <c:otherwise>
        <c:url var="itemBase" value="/item"/>
        <ul id="map-items" class="map-list">
          <c:forEach var="item" items="${mapItems}">
            <li data-lat="${item.location.latitude}" data-lng="${item.location.longitude}"
                data-type="<c:out value='${item.type}'/>">
              <span class="badge-inline badge-${item.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${item.type}"/></span>
              <a class="map-item-link" href="${itemBase}?id=${item.itemId}"><c:out value="${item.title}"/></a>
              <span class="map-item-meta">
                <c:out value="${item.categoryName}"/>
                <c:if test="${not empty item.location.locationName}"> &middot; <c:out value="${item.location.locationName}"/></c:if>
              </span>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </main>
</body>
</html>
