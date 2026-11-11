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
    <h1>Report inappropriate content</h1>
    <p class="subtitle">Tell us what is wrong. Your reason is shared only with administrators.</p>
    <c:if test="${sent}"><div class="notice" role="status">Your report was submitted for administrator review.</div></c:if>
    <c:if test="${not empty error}"><div class="form-error" role="alert"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty target}">
      <p>Reporting: <a href="${ctx}${target.path}"><c:out value="${target.label}"/></a></p>
      <c:choose>
        <c:when test="${not empty blockedReason}"><p class="notice" role="status"><c:out value="${blockedReason}"/></p></c:when>
        <c:when test="${not sent}">
          <form class="post-form" method="post" action="${ctx}/report/new">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
            <input type="hidden" name="type" value="<c:out value='${targetType}'/>" />
            <input type="hidden" name="id" value="${targetId}" />
            <div class="field">
              <label for="reason">Reason for reporting</label>
              <textarea id="reason" name="reason" rows="8" maxlength="${maxLength}" required aria-describedby="reason-hint reason-error" aria-invalid="${not empty reasonError}"><c:out value="${reason}"/></textarea>
              <small class="hint" id="reason-hint">Explain the issue in up to ${maxLength} characters.</small>
              <p class="field-error" id="reason-error"><c:out value="${reasonError}"/></p>
            </div>
            <button type="submit" class="btn" ${not empty error ? 'disabled' : ''}>Submit report</button>
          </form>
        </c:when>
      </c:choose>
      <p><a href="${ctx}${target.path}">Back to <c:out value="${targetType == 'item' ? 'post' : 'profile'}"/></a></p>
    </c:if>
    <c:if test="${empty target}"><p><a href="${ctx}/items">Browse items</a></p></c:if>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
