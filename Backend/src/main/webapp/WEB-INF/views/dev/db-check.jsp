<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- DEVELOPMENT ONLY. Rendering only: DbCheckServlet supplies connected, message, productName, productVersion, driverVersion. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Database Check | SpartanFinds (dev)</title>
  <style>
    body { font-family: system-ui, sans-serif; max-width: 40rem; margin: 2rem auto; padding: 0 1rem; line-height: 1.5; }
    .status { padding: 0.75rem 1rem; border-radius: 6px; font-weight: 600; }
    .ok { background: #e6f4ea; color: #1e6b34; }
    .fail { background: #fdecea; color: #a12a20; }
    dt { font-weight: 600; margin-top: 0.5rem; }
    dd { margin: 0; }
    .note { color: #555; font-size: 0.9rem; margin-top: 2rem; }
  </style>
</head>
<body>
  <h1>Database connection check</h1>

  <c:choose>
    <c:when test="${connected}">
      <p class="status ok" role="status">Connection OK</p>
      <dl>
        <dt>Server</dt><dd><c:out value="${productName}" /> <c:out value="${productVersion}" /></dd>
        <dt>JDBC driver</dt><dd><c:out value="${driverVersion}" /></dd>
      </dl>
    </c:when>
    <c:otherwise>
      <p class="status fail" role="alert">Connection failed</p>
      <p><c:out value="${message}" /></p>
    </c:otherwise>
  </c:choose>

  <p class="note">Development-only page, reachable from this machine only. Remove it before deploying anywhere else.</p>
</body>
</html>
