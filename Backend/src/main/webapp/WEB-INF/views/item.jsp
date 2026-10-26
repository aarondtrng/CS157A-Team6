<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: ItemPostServlet supplies post (or notImplemented) and loggedIn. Guests can view this page but see no actions. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title><c:choose><c:when test="${not empty post}"><c:out value="${post.title}"/></c:when><c:otherwise>Item</c:otherwise></c:choose> | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/item.css?v=${assetVersion}" />
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
          <div class="post-main">
            <%-- A post with no photos shows no gallery area at all, only the text below. --%>
            <c:choose>
              <c:when test="${not empty post.imageUrls}">
                <ul class="gallery" tabindex="0" aria-label="Item photos, scroll sideways for more">
                  <c:forEach var="url" items="${post.imageUrls}" varStatus="loop">
                    <li>
                      <figure>
                        <c:choose>
                          <c:when test="${url.startsWith('uploads/')}"><c:set var="photoSrc" value="${pageContext.request.contextPath}/${url}"/></c:when>
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
              </c:when>
            </c:choose>

            <div class="post-heading">
              <span class="badge-inline badge-${post.type == 'FOUND' ? 'found' : 'lost'}">
                <c:out value="${post.type}"/>
              </span>
              <h1><c:out value="${post.title}"/></h1>
              <p class="meta">
                <c:out value="${post.categoryName}"/> &middot;
                Posted <c:out value="${post.createdDateText}"/> &middot;
                Status: <c:out value="${post.status}"/>
              </p>
            </div>

            <section aria-labelledby="desc-heading">
              <h2 id="desc-heading">Description</h2>
              <p class="description"><c:out value="${post.description}"/></p>
            </section>
          </div>

          <aside class="post-side">
            <section class="side-card" aria-labelledby="poster-heading">
              <h2 id="poster-heading">Posted by</h2>
              <div class="poster">
                <c:choose>
                  <c:when test="${not empty post.posterProfileImage}">
                    <img class="avatar" src="<c:out value='${post.posterProfileImage}'/>" alt="" />
                  </c:when>
                  <c:otherwise>
                    <span class="avatar avatar-initials" aria-hidden="true"><c:out value="${post.posterInitials}"/></span>
                  </c:otherwise>
                </c:choose>
                <%-- TODO: link the name to the poster's profile page once profiles exist. --%>
                <span class="poster-name"><c:out value="${post.posterName}"/></span>
              </div>
              <%-- TODO: message and ownership-request actions once messaging and requests exist. --%>
            </section>

            <section class="side-card" aria-labelledby="location-heading">
              <h2 id="location-heading">Location</h2>
              <c:choose>
                <c:when test="${not empty post.location}">
                  <p><c:out value="${post.location.locationName}"/></p>
                  <p class="coords">
                    ${post.location.latitude}, ${post.location.longitude}
                  </p>
                  <%-- TODO: show this on the campus map once the map page exists. --%>
                </c:when>
                <c:otherwise>
                  <p>No location was given for this item.</p>
                </c:otherwise>
              </c:choose>
            </section>

            <c:choose>
              <c:when test="${loggedIn}">
                <%-- TODO: wire to the report form once reporting exists. Disabled until then. --%>
                <button type="button" class="btn btn-outline btn-block" disabled aria-disabled="true"
                        title="Reporting is coming soon">
                  Report this post
                </button>
              </c:when>
              <c:otherwise>
                <%-- TODO: link "Log in" once the login page exists. --%>
                <p class="guest-note">Log in to message the poster, request this item, or report this post.</p>
              </c:otherwise>
            </c:choose>
          </aside>
        </article>
      </c:otherwise>
    </c:choose>
  </main>
</body>
</html>
