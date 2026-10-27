<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Rendering only: CatalogServlet supplies query, sortOptions, categories, catalogPage, notImplemented. --%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/images/sjsuLogo.svg" />
  <title>Browse Items | SpartanFinds</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Geist:wght@400;500;600;700&amp;display=swap" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css?v=${assetVersion}" />
</head>
<body>
  <header class="topbar">
    <div class="container topbar-inner">
      <a class="brand" href="${pageContext.request.contextPath}/">
        <img class="brand-logo" src="${pageContext.request.contextPath}/images/sjsuLogo.svg" alt="SJSU Spartan logo" />
        SpartanFinds
      </a>
      <%-- TODO: nav links and profile link once those pages exist. --%>
      <%@ include file="/WEB-INF/views/fragments/topbar-auth.jspf" %>
    </div>
  </header>

  <main class="container">
    <c:choose>
      <c:when test="${notImplemented}">
        <div class="notice" role="status">
          <strong>Catalog data is not available yet.</strong>
          The database queries for browsing items have not been implemented
          (see <code>ItemDao</code>).
        </div>
      </c:when>

      <c:otherwise>
        <c:set var="ctx" value="${pageContext.request.contextPath}"/>
        <div class="catalog-layout">

          <%-- Filters: a GET form, so the server re-renders the list for the chosen category. --%>
          <form class="filters" method="get" action="${ctx}/items" aria-label="Filters">
            <input type="hidden" name="q" value="<c:out value='${query.searchText}'/>" />
            <input type="hidden" name="sort" value="${query.sort.param}" />
            <fieldset>
              <legend>CATEGORY</legend>
              <label class="radio-row">
                <input type="radio" name="category" value="" ${empty query.categoryId ? 'checked' : ''} /> All categories
              </label>
              <c:forEach var="cat" items="${categories}">
                <label class="radio-row">
                  <input type="radio" name="category" value="${cat.categoryId}" ${cat.categoryId == query.categoryId ? 'checked' : ''} />
                  <c:out value="${cat.categoryName}"/>
                </label>
              </c:forEach>
            </fieldset>
            <div class="filters-actions">
              <button type="submit" class="btn btn-small">Apply</button>
              <a href="${ctx}/items">Clear filters</a>
            </div>
          </form>

          <div class="posts">
            <section class="results" aria-label="Results">
            <%-- Sort is a dropdown in a GET form that keeps the search and category. js/catalog-sort.js submits it
                 when the choice changes; without JavaScript the Apply button does the same. --%>
            <div class="sortbar">
              <form class="sort-form" method="get" action="${ctx}/items">
                <input type="hidden" name="q" value="<c:out value='${query.searchText}'/>" />
                <input type="hidden" name="category" value="${query.categoryId}" />
                <label for="sort-select">Sort by</label>
                <select id="sort-select" name="sort">
                  <c:forEach var="option" items="${sortOptions}">
                    <option value="${option.param}" ${option == query.sort ? 'selected' : ''}><c:out value="${option.label}"/></option>
                  </c:forEach>
                </select>
                <noscript><button type="submit" class="btn btn-small">Apply</button></noscript>
              </form>
              <span class="result-count" role="status">
                <c:choose>
                  <c:when test="${not empty query.searchText}">${catalogPage.totalCount} result${catalogPage.totalCount == 1 ? '' : 's'} for &ldquo;<c:out value="${query.searchText}"/>&rdquo;</c:when>
                  <c:otherwise>${catalogPage.totalCount} item${catalogPage.totalCount == 1 ? '' : 's'}</c:otherwise>
                </c:choose>
              </span>
            </div>

            <c:choose>
              <c:when test="${empty catalogPage.items}">
                <div class="empty-results" role="status">No items match your search.</div>
              </c:when>
              <c:otherwise>
                <ul class="post-list">
                  <c:forEach var="item" items="${catalogPage.items}">
                    <c:url var="postUrl" value="/item">
                      <c:param name="id" value="${item.itemId}"/>
                    </c:url>
                    <li class="post-row">
                      <article class="post-card">
                        <div class="post-card-main">
                        <div class="post-card-body">
                          <h2><a class="card-link" href="${postUrl}"><c:out value="${item.title}"/></a></h2>
                          <div class="post-card-tags">
                            <span class="badge badge-${item.type == 'FOUND' ? 'found' : 'lost'}"><c:out value="${item.type}"/></span>
                            <span class="tag"><c:out value="${item.categoryName}"/></span>
                            <c:if test="${item.status == 'RETURNED'}"><span class="tag">Returned</span></c:if>
                          </div>
                          <p><c:out value="${item.briefDescription}"/></p>
                          <div class="post-card-meta">Posted <c:out value="${item.createdDateText}"/></div>
                        </div>
                        <%-- An item with no photo gets no image box at all. Uploaded photos are stored as
                             "uploads/<name>" and need the context path. --%>
                        <c:if test="${not empty item.thumbnailUrl}">
                          <c:choose>
                            <c:when test="${item.thumbnailUrl.startsWith('uploads/')}">
                              <c:set var="thumbSrc" value="${ctx}/${item.thumbnailUrl}"/>
                            </c:when>
                            <c:otherwise><c:set var="thumbSrc" value="${item.thumbnailUrl}"/></c:otherwise>
                          </c:choose>
                          <img class="post-card-thumb" src="<c:out value='${thumbSrc}'/>" alt="" loading="lazy" />
                        </c:if>
                        </div>
                        <%-- Message, Share and Report are shown but not clickable yet (they come with messaging and reports). --%>
                        <div class="post-card-actions">
                          <button type="button" class="act" disabled aria-disabled="true" title="Message is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z"></path></svg>Message</button>
                          <button type="button" class="act" disabled aria-disabled="true" title="Share is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12v7a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-7M16 6l-4-4-4 4M12 2v13"></path></svg>Share</button>
                          <button type="button" class="act" disabled aria-disabled="true" title="Report is coming soon"><svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 21V4M5 4h11l-2 4 2 4H5"></path></svg>Report</button>
                        </div>
                      </article>
                    </li>
                  </c:forEach>
                </ul>

                <nav class="pager" aria-label="Catalog pages">
                  <c:if test="${catalogPage.hasPrevious}">
                    <c:url var="prevUrl" value="/items">
                      <c:param name="q" value="${query.searchText}"/>
                      <c:param name="category" value="${query.categoryId}"/>
                      <c:param name="sort" value="${query.sort.param}"/>
                      <c:param name="page" value="${catalogPage.page - 1}"/>
                    </c:url>
                    <a href="${prevUrl}">Previous</a>
                  </c:if>
                  <span class="pager-current" aria-current="page">Page ${catalogPage.page} of ${catalogPage.totalPages}</span>
                  <c:if test="${catalogPage.hasNext}">
                    <c:url var="nextUrl" value="/items">
                      <c:param name="q" value="${query.searchText}"/>
                      <c:param name="category" value="${query.categoryId}"/>
                      <c:param name="sort" value="${query.sort.param}"/>
                      <c:param name="page" value="${catalogPage.page + 1}"/>
                    </c:url>
                    <a href="${nextUrl}">Next</a>
                  </c:if>
                </nav>
              </c:otherwise>
            </c:choose>
            </section>
          </div>

          <aside class="rail" aria-label="About">
            <section>
              <div class="rail-banner"></div>
              <div class="rail-body">
                <span class="rail-icon" aria-hidden="true">
                  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#0b1b2b" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="6"></circle><path d="M20 20l-4.5-4.5"></path></svg>
                </span>
                <h2>About SpartanFinds</h2>
                <p>The lost &amp; found board for San Jos&eacute; State. Post what you lost or found and claim items privately.</p>
              </div>
            </section>
            <section>
              <h2 class="rail-title">POSTING RULES</h2>
              <ol>
                <li><strong>1.</strong> Pin the spot where the item was lost or found.</li>
                <li><strong>2.</strong> Keep identifying details out of found posts.</li>
                <li><strong>3.</strong> Never post someone&rsquo;s personal information.</li>
                <li><strong>4.</strong> Mark your post returned once it&rsquo;s resolved.</li>
              </ol>
            </section>
          </aside>
        </div>
      </c:otherwise>
    </c:choose>
  </main>
  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
  <script src="${pageContext.request.contextPath}/js/catalog-sort.js?v=${assetVersion}" defer></script>
</body>
</html>
