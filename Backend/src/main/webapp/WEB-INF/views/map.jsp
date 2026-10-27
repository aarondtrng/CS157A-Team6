<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: MapServlet supplies mapItems. The item list below is real page content (it also works
     without JavaScript); js/items-map.js reads its data-* attributes to place the map markers. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Campus Map | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/map.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/map-pin.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/items-map.js?v=${assetVersion}" defer></script>
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

  <main class="container map-page">
    <c:set var="ctx" value="${pageContext.request.contextPath}"/>
    <div class="map-layout">
      <aside class="map-side" aria-label="Items on the map">
        <div class="map-side-head">
          <h1>Campus map</h1>
          <p>Pins mark where items were lost or found.</p>
          <div class="map-legend" aria-label="Pin colors">
            <span><i class="dot dot-lost"></i>Lost</span>
            <span><i class="dot dot-found"></i>Found</span>
          </div>
        </div>
        <c:choose>
          <c:when test="${empty mapItems}">
            <div class="map-empty">No items with a location have been posted yet.</div>
          </c:when>
          <c:otherwise>
            <c:url var="itemBase" value="/item"/>
            <ul id="map-items" class="map-list">
              <c:forEach var="item" items="${mapItems}">
                <%-- Uploaded photos are stored as "uploads/<name>" and need the context path. The popup photo is
                     read from data-photo by js/items-map.js. --%>
                <c:set var="photoSrc" value=""/>
                <c:if test="${not empty item.thumbnailUrl}">
                  <c:choose>
                    <c:when test="${item.thumbnailUrl.startsWith('uploads/')}"><c:set var="photoSrc" value="${ctx}/${item.thumbnailUrl}"/></c:when>
                    <c:otherwise><c:set var="photoSrc" value="${item.thumbnailUrl}"/></c:otherwise>
                  </c:choose>
                </c:if>
                <li data-lat="${item.location.latitude}" data-lng="${item.location.longitude}"
                    data-type="<c:out value='${item.type}'/>" data-photo="<c:out value='${photoSrc}'/>">
                  <a class="map-item" href="${itemBase}?id=${item.itemId}">
                    <i class="dot dot-${item.type == 'FOUND' ? 'found' : 'lost'}" aria-hidden="true"></i>
                    <span class="map-item-text">
                      <span class="map-item-link"><c:out value="${item.title}"/></span>
                      <span class="map-item-meta">
                        <c:out value="${item.type == 'FOUND' ? 'Found' : 'Lost'}"/> &middot; <c:out value="${item.categoryName}"/>
                        <c:if test="${not empty item.location.locationName}"> &middot; <c:out value="${item.location.locationName}"/></c:if>
                      </span>
                    </span>
                  </a>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </aside>

      <section class="map-frame" aria-label="Map">
        <div id="items-map" class="map map-large" role="application"
             aria-label="Campus map with pins for lost and found items. The same items are listed beside the map."></div>
      </section>
    </div>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
