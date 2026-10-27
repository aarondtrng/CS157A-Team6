<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: HomeServlet supplies recentItems (the newest real posts), popularCategories, and dataUnavailable. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>SpartanFinds | San Jos&eacute; State lost &amp; found</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/map-pin.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/home-map.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/home-hero-map.js?v=${assetVersion}" defer></script>
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
      <%-- The hero below has its own search box, so the header one is left out here. --%>
      <c:set var="hideNavSearch" value="true"/>
      <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
    </div>
  </header>

  <main>
    <c:set var="ctx" value="${pageContext.request.contextPath}"/>

    <section class="home-hero">
      <%-- A real map of the SJSU campus fills the hero (js/home-hero-map.js). It is only decoration, so it
           cannot be moved or clicked. The pins are the newest located posts, read from the hidden list. --%>
      <div id="hero-map" class="home-map" aria-hidden="true"></div>
      <ul id="hero-pins" hidden>
        <c:forEach var="pin" items="${heroPins}">
          <li data-lat="${pin.location.latitude}" data-lng="${pin.location.longitude}"
              data-type="<c:out value='${pin.type}'/>"></li>
        </c:forEach>
      </ul>
      <div class="home-scrim" aria-hidden="true"></div>

      <div class="home-hero-content">
        <p class="home-eyebrow"><span class="home-eyebrow-dot"></span>San Jos&eacute; State lost &amp; found</p>
        <h1>Lost it on campus?<br /><span class="home-accent">Someone found it.</span></h1>
        <p class="home-lede">Search posts from fellow Spartans, see exactly where items turned up on the campus map, and claim what&rsquo;s yours privately.</p>

        <form role="search" method="get" action="${ctx}/items" class="home-search">
          <label for="hero-q" class="visually-hidden">Search items</label>
          <input id="hero-q" name="q" type="search" maxlength="100" placeholder="Try &ldquo;AirPods&rdquo;, &ldquo;student ID&rdquo;, &ldquo;blue backpack&rdquo;" />
          <button type="submit" class="btn">Search</button>
        </form>

        <c:if test="${not empty popularCategories}">
          <div class="home-popular">
            <span>Popular:</span>
            <c:forEach var="cat" items="${popularCategories}">
              <a class="home-chip" href="${ctx}/items?category=${cat.categoryId}"><c:out value="${cat.categoryName}"/></a>
            </c:forEach>
          </div>
        </c:if>
      </div>
    </section>

    <section class="container home-section" aria-labelledby="recent-heading">
      <div class="home-section-head">
        <div>
          <h2 id="recent-heading" class="home-title">Recently posted</h2>
          <p class="home-sub">The newest lost and found reports from around campus.</p>
        </div>
        <a class="home-viewall" href="${ctx}/items">View all items
          <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
        </a>
      </div>

      <c:choose>
        <c:when test="${dataUnavailable}">
          <div class="notice" role="status">Recent posts could not be loaded right now. You can still <a href="${ctx}/items">browse all items</a>.</div>
        </c:when>
        <c:when test="${empty recentItems}">
          <div class="notice" role="status">No posts yet. Be the first to <a href="${ctx}/post/new">create one</a>.</div>
        </c:when>
        <c:otherwise>
          <ul class="home-grid">
            <c:forEach var="item" items="${recentItems}">
              <c:url var="postUrl" value="/item"><c:param name="id" value="${item.itemId}"/></c:url>
              <li>
                <a class="home-card" href="${postUrl}">
                  <%-- A post with no photo gets no photo box: the badge moves into the text. Uploaded photos are
                       stored as "uploads/<name>" and need the context path. --%>
                  <c:if test="${not empty item.thumbnailUrl}">
                    <c:choose>
                      <c:when test="${item.thumbnailUrl.startsWith('uploads/')}"><c:set var="thumbSrc" value="${ctx}/${item.thumbnailUrl}"/></c:when>
                      <c:otherwise><c:set var="thumbSrc" value="${item.thumbnailUrl}"/></c:otherwise>
                    </c:choose>
                    <span class="home-photo">
                      <img src="<c:out value='${thumbSrc}'/>" alt="" loading="lazy" />
                      <span class="badge badge-${item.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${item.type}"/></span>
                    </span>
                  </c:if>
                  <span class="home-card-body">
                    <c:if test="${empty item.thumbnailUrl}">
                      <span class="badge badge-${item.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${item.type}"/></span>
                    </c:if>
                    <span class="home-card-cat"><c:out value="${item.categoryName}"/></span>
                    <span class="home-card-title"><c:out value="${item.title}"/></span>
                    <span class="home-card-meta">Posted <c:out value="${item.createdDateText}"/></span>
                  </span>
                </a>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <section id="how" class="container home-section" aria-labelledby="how-heading">
      <h2 id="how-heading" class="home-title">How it works</h2>
      <ol class="home-steps">
        <li><span class="home-step-n">01</span><h3>Post what you lost or found</h3><p>Add a title, photos and a category, then drop a pin where it happened.</p></li>
        <li><span class="home-step-n">02</span><h3>Search and match</h3><p>Filter by category, sort by date, or browse pins on the campus map.</p></li>
        <li><span class="home-step-n">03</span><h3>Claim it privately</h3><p>Send ownership details only the finder sees, then arrange the return.</p></li>
      </ol>
    </section>

    <section class="container home-section home-section-last" aria-labelledby="map-heading">
      <div class="home-mapcard">
        <div class="home-mapcard-text">
          <h2 id="map-heading" class="home-title">See it on the map</h2>
          <p class="home-sub">Every post is pinned where the item was lost or found &mdash; retrace your steps from the library to the Event Center.</p>
          <a class="btn btn-dark" href="${ctx}/map">Open campus map</a>
        </div>
        <c:choose>
          <c:when test="${not empty latestPin}">
            <%-- js/home-map.js draws the pin from these data attributes (no request to our server). --%>
            <div class="home-mapprev">
              <div id="home-map" class="home-map-live" role="img"
                   data-lat="${latestPin.location.latitude}" data-lng="${latestPin.location.longitude}"
                   data-type="<c:out value='${latestPin.type}'/>"
                   aria-label="Map showing where the newest post was ${latestPin.type == 'FOUND' ? 'found' : 'lost'}"></div>
              <c:url var="latestUrl" value="/item"><c:param name="id" value="${latestPin.itemId}"/></c:url>
              <a class="home-latest" href="${latestUrl}">
                <span class="badge badge-${latestPin.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${latestPin.type}"/></span>
                <strong><c:out value="${latestPin.title}"/></strong>
                <span class="home-latest-meta">
                  <c:out value="${latestPin.categoryName}"/>
                  <c:if test="${not empty latestPin.location.locationName}"> &middot; <c:out value="${latestPin.location.locationName}"/></c:if>
                </span>
                <span class="home-latest-link">Newest post &middot; View post &rarr;</span>
              </a>
            </div>
          </c:when>
          <c:otherwise>
            <div class="home-mapprev" role="img" aria-label="Campus map preview">
              <span class="home-block" style="left: 18%; top: 22%; width: 24%; height: 20%"></span>
              <span class="home-block" style="left: 52%; top: 18%; width: 30%; height: 26%"></span>
              <span class="home-block" style="left: 28%; top: 56%; width: 34%; height: 22%"></span>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <section class="home-cta">
      <div class="container home-cta-inner">
        <div>
          <h2>Found something that isn&rsquo;t yours?</h2>
          <p>Posting takes a minute. Sign in with your SJSU email.</p>
        </div>
        <div class="home-cta-actions">
          <a class="btn btn-gold" href="${ctx}/post/new?type=FOUND">Report a found item</a>
          <a class="btn btn-ghost" href="${ctx}/post/new?type=LOST">Report a lost item</a>
        </div>
      </div>
    </section>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
