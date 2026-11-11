<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Reports | SpartanFinds</title>
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/post.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reports.css?v=${assetVersion}" />
</head>
<body>
  <c:set var="ctx" value="${pageContext.request.contextPath}"/>
  <header class="topbar"><div class="container topbar-inner">
    <a class="brand" href="${ctx}/"><img class="brand-logo" src="${ctx}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />SpartanFinds</a>
    <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
  </div></header>
  <main class="container reports-main">
    <h1>Reported content</h1>
    <p class="subtitle">Review reports, then resolve or dismiss them. Resolving records your decision; it does not remove content.</p>
    <form method="get" action="${ctx}/admin/reports" class="report-filter">
      <label for="status">Status</label>
      <select name="status" id="status">
        <c:forEach var="option" items="${['PENDING', 'RESOLVED', 'DISMISSED', 'ALL']}">
          <option value="${option}" ${statusFilter == option ? 'selected' : ''}><c:out value="${option}"/></option>
        </c:forEach>
      </select>
      <button class="btn btn-outline" type="submit">Filter</button>
    </form>
    <c:if test="${not empty queueMessage}"><p class="notice" role="status"><c:out value="${queueMessage}"/></p></c:if>
    <c:choose>
      <c:when test="${not empty error}"><p class="form-error" role="alert"><c:out value="${error}"/></p></c:when>
      <c:when test="${empty reports}"><p class="notice">No reports match this status.</p></c:when>
      <c:otherwise>
        <p>Showing up to 100 reports, newest first.</p>
        <ul class="report-list">
          <c:forEach var="report" items="${reports}">
            <li class="panel report-card">
              <div class="report-heading"><h2>Report #${report.reportId}</h2><span class="tag"><c:out value="${report.status}"/></span></div>
              <p>Submitted by <a href="${ctx}/profile?id=${report.reporterId}"><c:out value="${report.reporterName}"/></a> on <c:out value="${report.reportDateText}"/></p>
              <p>Reported user: <a href="${ctx}/profile?id=${report.reportedUserId}"><c:out value="${report.reportedUserName}"/></a></p>
              <c:if test="${not empty report.itemId}"><p>Reported post: <a href="${ctx}/item?id=${report.itemId}">Post #${report.itemId}</a></p></c:if>
              <h3>Reason</h3><p class="report-reason"><c:out value="${report.reason}"/></p>
              <c:if test="${report.pending}">
                <form method="post" action="${ctx}/admin/reports/action" class="report-actions">
                  <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
                  <input type="hidden" name="reportId" value="${report.reportId}" />
                  <input type="hidden" name="status" value="<c:out value='${statusFilter}'/>" />
                  <button type="submit" class="btn" name="action" value="resolve">Resolve</button>
                  <button type="submit" class="btn btn-outline" name="action" value="dismiss">Dismiss</button>
                </form>
              </c:if>
            </li>
          </c:forEach>
        </ul>
      </c:otherwise>
    </c:choose>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
