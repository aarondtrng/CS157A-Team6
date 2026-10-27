<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: RequestsServlet supplies made and received (lists of RequestRow), csrfToken, and the one-time flags sent, updated, failed. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>My requests | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/requests.css?v=${assetVersion}" />
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
    <a class="back-link" href="${pageContext.request.contextPath}/items">&larr; Back to all items</a>
    <h1>My requests</h1>
    <p class="subtitle">Requests you sent for found items, and requests other students sent for items you found.</p>

    <c:if test="${sent}"><div class="flash flash-ok" role="status">Your request was sent. The student who found the item will review it.</div></c:if>
    <c:if test="${updated}"><div class="flash flash-ok" role="status">The request was updated.</div></c:if>
    <c:if test="${failed}"><div class="flash flash-bad" role="alert">That change could not be made. The request may have been updated already.</div></c:if>

    <section aria-labelledby="received-heading">
      <h2 id="received-heading">Requests on my posts</h2>
      <p class="section-note">The details below are private. Ask the student for anything only the owner would know, then accept and hand the item back.</p>
      <c:choose>
        <c:when test="${empty received}">
          <p class="empty">Nobody has sent a request for your posts yet.</p>
        </c:when>
        <c:otherwise>
          <ul class="request-list received">
            <c:forEach var="row" items="${received}">
              <li class="request-card">
                <div class="request-head">
                  <a class="request-title" href="${pageContext.request.contextPath}/item?id=${row.itemId}"><c:out value="${row.itemTitle}"/></a>
                  <span class="status status-${row.status.toLowerCase()}"><c:out value="${row.status}"/></span>
                </div>
                <p class="request-meta">
                  From <a href="${pageContext.request.contextPath}/profile?id=${row.otherUserId}"><c:out value="${row.otherUserName}"/></a>
                  &middot; <c:out value="${row.createdDateText}"/>
                </p>
                <p class="request-evidence"><c:out value="${row.description}"/></p>

                <c:if test="${row.active}">
                  <div class="request-actions">
                    <c:if test="${row.pending}">
                      <form method="post" action="${pageContext.request.contextPath}/requests/action">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
                        <input type="hidden" name="requestId" value="${row.requestId}" />
                        <input type="hidden" name="action" value="accept" />
                        <button type="submit" class="btn btn-small">Accept</button>
                      </form>
                    </c:if>
                    <c:if test="${row.accepted}">
                      <form method="post" action="${pageContext.request.contextPath}/requests/action">
                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
                        <input type="hidden" name="requestId" value="${row.requestId}" />
                        <input type="hidden" name="action" value="complete" />
                        <button type="submit" class="btn btn-gold btn-small">Complete return</button>
                      </form>
                    </c:if>
                    <form method="post" action="${pageContext.request.contextPath}/requests/action">
                      <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
                      <input type="hidden" name="requestId" value="${row.requestId}" />
                      <input type="hidden" name="action" value="reject" />
                      <button type="submit" class="btn btn-danger btn-small">Reject</button>
                    </form>
                  </div>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>

    <section aria-labelledby="made-heading">
      <h2 id="made-heading">Requests I sent</h2>
      <c:choose>
        <c:when test="${empty made}">
          <p class="empty">You have not requested any items. Open a found item and choose &ldquo;This is mine&rdquo;.</p>
        </c:when>
        <c:otherwise>
          <ul class="request-list made">
            <c:forEach var="row" items="${made}">
              <li class="request-card">
                <div class="request-head">
                  <a class="request-title" href="${pageContext.request.contextPath}/item?id=${row.itemId}"><c:out value="${row.itemTitle}"/></a>
                  <span class="status status-${row.status.toLowerCase()}"><c:out value="${row.status}"/></span>
                </div>
                <p class="request-meta">
                  Found by <a href="${pageContext.request.contextPath}/profile?id=${row.otherUserId}"><c:out value="${row.otherUserName}"/></a>
                  &middot; sent <c:out value="${row.createdDateText}"/>
                </p>
                <p class="request-evidence"><c:out value="${row.description}"/></p>
                <c:if test="${row.active}">
                  <div class="request-actions">
                    <form method="post" action="${pageContext.request.contextPath}/requests/action">
                      <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
                      <input type="hidden" name="requestId" value="${row.requestId}" />
                      <input type="hidden" name="action" value="cancel" />
                      <button type="submit" class="btn btn-outline btn-small">Cancel request</button>
                    </form>
                  </div>
                </c:if>
              </li>
            </c:forEach>
          </ul>
        </c:otherwise>
      </c:choose>
    </section>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
