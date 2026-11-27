// Messages page helpers: shows how many photos are picked, a live character count, sends messages without
// reloading the page, and checks for new messages every few seconds. Everything still works without this
// script (the form posts and redirects back, and the Refresh link reloads the page).
(function () {
  var POLL_MS = 3000;

  var form = document.getElementById('composer');
  var input = document.getElementById('msg-images');
  var label = document.getElementById('msg-images-label');
  var text = document.getElementById('msg-text');
  var counter = document.getElementById('msg-count');

  function updatePhotoLabel() {
    if (!input || !label) {
      return;
    }
    var count = input.files.length;
    label.textContent = count === 0 ? 'Photo' : count === 1 ? '1 photo' : count + ' photos';
  }

  if (input && label) {
    input.addEventListener('change', updatePhotoLabel);
  }

  var updateCount = function () {};
  if (text && counter) {
    var max = text.maxLength > 0 ? text.maxLength : 5000;
    var warnAt = max - 200;
    updateCount = function () {
      var length = text.value.length;
      counter.textContent = length.toLocaleString() + ' / ' + max.toLocaleString();
      counter.classList.toggle('near-limit', length >= warnAt);
    };
    text.addEventListener('input', updateCount);
    updateCount();
    counter.hidden = false;
  }

  if (!window.fetch || !window.FormData || !window.DOMParser) {
    return;
  }

  // ---- Shared: apply a freshly rendered copy of the page ----

  // Every request gets a number; a response is only applied if nothing newer was applied first, so a slow
  // background check can never put back an older list over a message that was just sent.
  var nextRequest = 0;
  var lastApplied = 0;

  function parsePage(html) {
    var doc = new DOMParser().parseFromString(html, 'text/html');
    // Anything else (for example the login page after the session ran out) is not usable.
    return doc.querySelector('.convo-list') ? doc : null;
  }

  // Replaces the element matching selector with the fresh copy, only when its contents actually changed.
  function swap(doc, selector) {
    var fresh = doc.querySelector(selector);
    var current = document.querySelector(selector);
    if (fresh && current && fresh.innerHTML !== current.innerHTML) {
      current.replaceWith(document.importNode(fresh, true));
    }
  }

  // Swaps in the conversation and the inbox. If the message box is on screen it stays in the same spot while
  // the list above it grows; if the student scrolled up to read older messages, nothing moves.
  function applyPage(doc) {
    var anchor = form || document.querySelector('.thread');
    var top = anchor ? anchor.getBoundingClientRect().top : null;
    var anchorVisible = top !== null && top < window.innerHeight;

    swap(doc, '.bubbles');
    swap(doc, '.convo-list ul');

    if (anchorVisible) {
      window.scrollBy(0, anchor.getBoundingClientRect().top - top);
    }
  }

  // ---- Sending without a reload ----

  var sending = false;

  if (form) {
    var button = form.querySelector('button[type="submit"]');

    // Puts an error just above the composer, replacing any earlier one; null just clears it.
    var showFlash = function (flash) {
      var old = document.querySelector('.thread .flash');
      if (old) {
        old.remove();
      }
      if (flash) {
        form.parentNode.insertBefore(flash, form);
      }
    };

    var plainFlash = function (message) {
      var div = document.createElement('div');
      div.className = 'flash flash-bad';
      div.setAttribute('role', 'alert');
      div.textContent = message;
      return div;
    };

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      if (sending) {
        return;
      }
      sending = true;
      if (button) {
        button.disabled = true;
      }
      var request = ++nextRequest;

      // The server answers with a redirect back to the conversation; fetch follows it and gets that page.
      fetch(form.action, { method: 'POST', body: new FormData(form), credentials: 'same-origin' })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('HTTP ' + response.status);
          }
          return response.text();
        })
        .then(function (html) {
          var doc = parsePage(html);
          if (!doc || !doc.querySelector('.bubbles')) {
            throw new Error('Unexpected page');
          }
          var flash = doc.querySelector('.thread .flash');
          if (request > lastApplied) {
            lastApplied = request;
            applyPage(doc);
          }
          showFlash(flash ? document.importNode(flash, true) : null);
          if (!flash) {
            // Sent: clear the box and the photo picker. On an error the text is kept so it can be fixed.
            form.reset();
            updatePhotoLabel();
            updateCount();
          }
        })
        .catch(function () {
          showFlash(plainFlash('The message could not be sent. Refresh the page and try again.'));
        })
        .then(function () {
          sending = false;
          if (button) {
            button.disabled = false;
          }
          if (text) {
            text.focus({ preventScroll: true });
          }
        });
    });
  }

  // ---- Checking for new messages ----

  // The Refresh link already points at this page without any one-time ?error= part.
  var refreshLink = document.querySelector('.convo-head a.act');
  if (!refreshLink) {
    return;
  }
  var pollUrl = refreshLink.href;
  var polling = false;
  var stopped = false;
  var timer = null;

  function schedule() {
    clearTimeout(timer);
    if (!stopped && !document.hidden) {
      timer = setTimeout(poll, POLL_MS);
    }
  }

  function poll() {
    // Opening a conversation marks it read, so never check while the tab is hidden (the other student would
    // see "Seen" on messages nobody looked at). Also skip while a send is already bringing fresh data.
    if (stopped || document.hidden || polling || sending) {
      schedule();
      return;
    }
    polling = true;
    var request = ++nextRequest;
    fetch(pollUrl, { credentials: 'same-origin', cache: 'no-store' })
      .then(function (response) {
        if (!response.ok) {
          throw new Error('HTTP ' + response.status);
        }
        return response.text();
      })
      .then(function (html) {
        var doc = parsePage(html);
        if (!doc) {
          // Signed out or the conversation is gone: stop quietly; the next send or refresh shows why.
          stopped = true;
          return;
        }
        if (request > lastApplied) {
          lastApplied = request;
          applyPage(doc);
        }
      })
      .catch(function () {
        // A dropped connection: just try again next time.
      })
      .then(function () {
        polling = false;
        schedule();
      });
  }

  document.addEventListener('visibilitychange', function () {
    if (document.hidden) {
      clearTimeout(timer);
    } else {
      poll();
    }
  });

  schedule();
})();
