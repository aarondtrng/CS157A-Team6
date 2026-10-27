<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: ItemPostServlet supplies post (or notImplemented) and loggedIn. Guests can view this page but see no actions. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title><c:choose><c:when test="${not empty post}"><c:out value="${post.title}"/></c:when><c:otherwise>Item</c:otherwise></c:choose> | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/item.css?v=${assetVersion}" />
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin="" defer></script>
  <script src="${pageContext.request.contextPath}/js/map-pin.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/item-map.js?v=${assetVersion}" defer></script>
  <script src="${pageContext.request.contextPath}/js/item-gallery.js?v=${assetVersion}" defer></script>
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
    <nav class="breadcrumb" aria-label="Breadcrumb">
      <a href="${ctx}/items">Browse items</a>
      <c:if test="${not empty post}"><span aria-hidden="true">/</span><span><c:out value="${post.categoryName}"/></span></c:if>
    </nav>

    <c:choose>
      <c:when test="${notImplemented}">
        <div class="notice" role="status">
          <strong>This post is not available yet.</strong>
          The database query for loading a post has not been implemented
          (see <code>ItemDao.findPublicItemById</code>).
        </div>
      </c:when>

      <c:otherwise>
        <article class="post">
          <%-- A post with no photos shows no gallery card at all. --%>
          <c:if test="${not empty post.imageUrls}">
            <section class="post-photos panel" aria-label="Photos">
              <ul class="gallery" tabindex="0" aria-label="Item photos, scroll sideways for more">
                <c:forEach var="url" items="${post.imageUrls}" varStatus="loop">
                  <li>
                    <figure>
                      <c:choose>
                        <c:when test="${url.startsWith('uploads/')}"><c:set var="photoSrc" value="${ctx}/${url}"/></c:when>
                        <c:otherwise><c:set var="photoSrc" value="${url}"/></c:otherwise>
                      </c:choose>
                      <img src="<c:out value='${photoSrc}'/>"
                           alt="<c:out value='${post.title}'/>, photo ${loop.count} of ${post.imageUrls.size()}" />
                      <c:if test="${post.imageUrls.size() > 1}">
                        <figcaption>Photo ${loop.count} of ${post.imageUrls.size()}</figcaption>
                      </c:if>
                    </figure>
                  </li>
                </c:forEach>
              </ul>
              <%-- With more than one photo, a row of small buttons jumps between them (js/item-gallery.js). --%>
              <c:if test="${post.imageUrls.size() > 1}">
                <div class="gallery-thumbs" role="group" aria-label="Choose a photo">
                  <c:forEach var="url" items="${post.imageUrls}" varStatus="loop">
                    <c:choose>
                      <c:when test="${url.startsWith('uploads/')}"><c:set var="thumbSrc" value="${ctx}/${url}"/></c:when>
                      <c:otherwise><c:set var="thumbSrc" value="${url}"/></c:otherwise>
                    </c:choose>
                    <button type="button" class="gallery-thumb" data-index="${loop.index}"
                            aria-label="Photo ${loop.count} of ${post.imageUrls.size()}" aria-pressed="${loop.first}">
                      <img src="<c:out value='${thumbSrc}'/>" alt="" loading="lazy" />
                    </button>
                  </c:forEach>
                </div>
              </c:if>
            </section>
          </c:if>

          <div class="post-side panel">
            <section class="side-card" aria-labelledby="poster-heading">
              <h2 id="poster-heading" class="visually-hidden">Posted by</h2>
              <div class="poster">
                <c:choose>
                  <c:when test="${not empty post.posterProfileImage}">
                    <img class="avatar" src="<c:out value='${post.posterProfileImage}'/>" alt="" />
                  </c:when>
                  <c:otherwise>
                    <span class="avatar avatar-initials" aria-hidden="true"><c:out value="${post.posterInitials}"/></span>
                  </c:otherwise>
                </c:choose>
                <div class="poster-text">
                  <a class="poster-name" href="${ctx}/profile?id=${post.posterUserId}"><c:out value="${post.posterName}"/></a>
                  <span class="poster-role">Posted this item</span>
                </div>
                <%-- Shown but not clickable yet; messaging is not built. --%>
                <button type="button" class="act act-outline" disabled aria-disabled="true"
                        title="Message is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z"></path></svg>Message</button>
              </div>
            </section>

            <section class="post-main">
              <div class="post-tags">
                <span class="badge badge-${post.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${post.type}"/></span>
                <span class="tag"><c:out value="${post.categoryName}"/></span>
                <c:choose>
                  <c:when test="${post.status == 'ACTIVE'}"><span class="tag tag-open">Open</span></c:when>
                  <c:when test="${post.status == 'RETURNED'}"><span class="tag">Returned</span></c:when>
                  <c:otherwise><span class="tag"><c:out value="${post.status}"/></span></c:otherwise>
                </c:choose>
              </div>
              <h1><c:out value="${post.title}"/></h1>
              <p class="meta">Posted <c:out value="${post.createdDateText}"/></p>
              <p class="description"><c:out value="${post.description}"/></p>

              <%-- Ownership requests: only on found posts, only for students. The creator manages requests;
                   everyone else can ask for the item unless it was already returned. --%>
              <c:if test="${post.type == 'FOUND' and isStudent}">
                <div class="claim">
                  <c:choose>
                    <c:when test="${isOwner}">
                      <a class="btn btn-lg" href="${ctx}/requests">View ownership requests</a>
                      <p class="claim-note">Students who say this item is theirs appear on your requests page.</p>
                    </c:when>
                    <c:when test="${not empty myRequestStatus}">
                      <a class="btn btn-outline btn-lg" href="${ctx}/requests">Your request is <c:out value="${myRequestStatus}"/></a>
                      <p class="claim-note">You can follow it on your requests page.</p>
                    </c:when>
                    <c:when test="${post.status == 'ACTIVE'}">
                      <a class="btn btn-lg" href="${ctx}/request/new?itemId=${post.itemId}">This is mine &mdash; request it</a>
                      <p class="claim-note">Your proof of ownership is private and only shared with the finder.</p>
                    </c:when>
                    <c:otherwise>
                      <p class="claim-note">This item is no longer available to request.</p>
                    </c:otherwise>
                  </c:choose>
                </div>
              </c:if>
              <c:if test="${empty loggedIn or not loggedIn}">
                <div class="claim">
                  <%-- "next" is a path inside the app (no context path): the login page adds the context path itself. --%>
                  <c:url var="loginUrl" value="/login"><c:param name="next" value="/item?id=${post.itemId}"/></c:url>
                  <a class="btn btn-lg" href="${loginUrl}">Log in to respond</a>
                  <p class="claim-note">Guests can browse posts and the map. Log in with your SJSU email to request this item or report the post.</p>
                </div>
              </c:if>
            </section>

            <section class="side-card" aria-labelledby="location-heading">
              <h2 id="location-heading">Where it was ${post.type == 'FOUND' ? 'found' : 'lost'}</h2>
              <c:choose>
                <c:when test="${not empty post.location}">
                  <p class="place">
                    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21z"></path><circle cx="12" cy="9.5" r="2.5"></circle></svg>
                    <c:choose>
                      <c:when test="${not empty post.location.locationName}"><c:out value="${post.location.locationName}"/></c:when>
                      <c:otherwise>Marked on the campus map</c:otherwise>
                    </c:choose>
                  </p>
                  <%-- js/item-map.js draws the pin from these data attributes (no request to our server). --%>
                  <div id="item-map" class="item-map" role="img"
                       data-lat="${post.location.latitude}" data-lng="${post.location.longitude}"
                       data-type="<c:out value='${post.type}'/>"
                       aria-label="Map showing where the item was ${post.type == 'FOUND' ? 'found' : 'lost'}"></div>
                  <p class="coords">${post.location.latitude}, ${post.location.longitude}</p>
                </c:when>
                <c:otherwise>
                  <p>No location was given for this item.</p>
                </c:otherwise>
              </c:choose>
            </section>

            <%-- Share and Report are shown but not clickable yet (sharing and reports are not built). --%>
            <div class="report-row">
              <button type="button" class="act" disabled aria-disabled="true" title="Share is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7M16 6l-4-4-4 4M12 2v13"></path></svg>Share</button>
              <button type="button" class="act" disabled aria-disabled="true" title="Report is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 21V4M5 4h11l-2 4 2 4H5"></path></svg>Report</button>
            </div>
          </div>

          <%-- Other posts in the same category (or the newest posts, when it has none) fill the space on the right. --%>
          <c:if test="${not empty relatedItems}">
            <aside class="related panel" aria-labelledby="related-heading">
              <h2 id="related-heading">
                <c:choose>
                  <c:when test="${relatedFallback}">More items</c:when>
                  <c:otherwise>More in <c:out value="${post.categoryName}"/></c:otherwise>
                </c:choose>
              </h2>
              <ul class="related-list">
                <c:forEach var="other" items="${relatedItems}">
                  <c:url var="otherUrl" value="/item"><c:param name="id" value="${other.itemId}"/></c:url>
                  <li>
                    <a class="related-item" href="${otherUrl}">
                      <span class="related-text">
                        <span class="related-type related-type-${other.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${other.type}"/></span>
                        <span class="related-title"><c:out value="${other.title}"/></span>
                        <span class="related-meta"><c:out value="${other.createdDateText}"/></span>
                      </span>
                      <%-- The photo is on the right, and a post with no photo gets no box at all. --%>
                      <c:if test="${not empty other.thumbnailUrl}">
                        <c:choose>
                          <c:when test="${other.thumbnailUrl.startsWith('uploads/')}"><c:set var="otherSrc" value="${ctx}/${other.thumbnailUrl}"/></c:when>
                          <c:otherwise><c:set var="otherSrc" value="${other.thumbnailUrl}"/></c:otherwise>
                        </c:choose>
                        <img class="related-thumb" src="<c:out value='${otherSrc}'/>" alt="" loading="lazy" />
                      </c:if>
                    </a>
                  </li>
                </c:forEach>
              </ul>
              <c:choose>
                <c:when test="${relatedFallback}"><a class="related-all" href="${ctx}/items">See all items</a></c:when>
                <c:otherwise><a class="related-all" href="${ctx}/items?category=${post.categoryId}">See all in <c:out value="${post.categoryName}"/></a></c:otherwise>
              </c:choose>
            </aside>
          </c:if>
        </article>
      </c:otherwise>
    </c:choose>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
