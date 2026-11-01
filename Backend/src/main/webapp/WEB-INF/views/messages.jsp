<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: MessagesServlet supplies conversations (inbox rows), selected (the open conversation or null),
     thread (its messages, oldest first), csrfToken and sendError. Every message and name is escaped with c:out. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Messages | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/messages.css?v=${assetVersion}" />
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
    <div class="messages panel">

      <section class="convo-list" aria-label="Conversations">
        <div class="convo-head">
          <h1>Messages</h1>
          <c:url var="refreshUrl" value="/messages"><c:if test="${not empty selected}"><c:param name="c" value="${selected.conversationId}"/></c:if></c:url>
          <a class="act" href="${refreshUrl}"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 11a8 8 0 1 0-2.3 5.7"></path><path d="M20 4v7h-7"></path></svg>Refresh</a>
        </div>
        <c:choose>
          <c:when test="${empty conversations}">
            <p class="convo-empty">No conversations yet.</p>
          </c:when>
          <c:otherwise>
            <ul>
              <c:forEach var="cv" items="${conversations}">
                <c:url var="convoUrl" value="/messages"><c:param name="c" value="${cv.conversationId}"/></c:url>
                <c:set var="isCurrent" value="${not empty selected and cv.conversationId == selected.conversationId}"/>
                <li>
                  <a class="convo${isCurrent ? ' convo-current' : ''}${cv.unread ? ' convo-unread' : ''}" href="${convoUrl}" ${isCurrent ? 'aria-current="page"' : ''}>
                    <span class="av av-${cv.avatarColor}" aria-hidden="true"><c:out value="${cv.initials}"/></span>
                    <span class="convo-text">
                      <span class="convo-top">
                        <span class="convo-name"><c:out value="${cv.otherName}"/></span>
                        <span class="convo-when"><c:out value="${cv.when}"/></span>
                      </span>
                      <span class="convo-bottom">
                        <span class="convo-preview"><c:out value="${cv.preview}"/></span>
                        <c:if test="${cv.unread}"><span class="convo-badge">${cv.unreadCount}<span class="visually-hidden"> unread</span></span></c:if>
                      </span>
                    </span>
                  </a>
                </li>
              </c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
        <p class="convo-note">Start a conversation from a student&rsquo;s profile.</p>
      </section>

      <section class="thread" aria-label="Conversation${not empty selected ? ' with ' : ''}<c:out value='${selected.otherName}'/>">
        <c:choose>
          <c:when test="${empty selected}">
            <div class="thread-empty">
              <h2>No conversation open</h2>
              <p>Use the Message button on a student&rsquo;s profile or post to start one.</p>
            </div>
          </c:when>
          <c:otherwise>
            <div class="thread-head">
              <span class="av av-${selected.avatarColor}" aria-hidden="true"><c:out value="${selected.initials}"/></span>
              <div class="thread-who">
                <a href="${ctx}/profile?id=${selected.otherUserId}"><c:out value="${selected.otherName}"/></a>
                <span>Student</span>
              </div>
              <a class="btn btn-outline btn-small" href="${ctx}/profile?id=${selected.otherUserId}">View profile</a>
              <%-- Shown but not clickable yet; reports are not built. --%>
              <button type="button" class="act" disabled aria-disabled="true" title="Report is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 21V4M5 4h11l-2 4 2 4H5"></path></svg>Report</button>
            </div>

            <ol class="bubbles" aria-label="Messages">
              <c:if test="${empty thread}">
                <li class="day-label">NO MESSAGES YET</li>
              </c:if>
              <c:forEach var="msg" items="${thread}">
                <c:if test="${not empty msg.dayLabel}"><li class="day-label"><c:out value="${msg.dayLabel}"/></li></c:if>
                <li class="bubble-row ${msg.mine ? 'bubble-mine' : 'bubble-theirs'}">
                  <span class="bubble"><c:out value="${msg.text}"/></span>
                  <span class="bubble-meta"><c:out value="${msg.mine ? 'You' : selected.otherName}"/> &middot; <c:out value="${msg.timeText}"/><c:if test="${msg.seen}"> &middot; Seen</c:if></span>
                </li>
              </c:forEach>
            </ol>

            <c:if test="${not empty sendError}">
              <div class="flash flash-bad" role="alert">
                <c:choose>
                  <c:when test="${sendError == 'empty'}">Write something before sending.</c:when>
                  <c:when test="${sendError == 'long'}">That message is too long. Please keep it under 5000 characters.</c:when>
                  <c:when test="${sendError == 'chars'}">The message contains characters that are not allowed.</c:when>
                  <c:otherwise>The message could not be sent.</c:otherwise>
                </c:choose>
              </div>
            </c:if>
            <form class="composer" method="post" action="${ctx}/messages/send">
              <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>" />
              <input type="hidden" name="conversationId" value="${selected.conversationId}" />
              <label class="visually-hidden" for="msg-text">Message</label>
              <textarea id="msg-text" name="text" rows="1" maxlength="5000" placeholder="Write a message&hellip;" required></textarea>
              <button type="submit" class="btn">Send</button>
            </form>
            <p class="thread-note">New messages appear when you refresh the page.</p>
          </c:otherwise>
        </c:choose>
      </section>
    </div>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
