// Messages page helpers: shows how many photos are picked, a live character count, sends messages without
// reloading the page, and checks for new messages every few seconds. Everything still works without this
// script (the form posts and redirects back, and the Refresh link reloads the page).
(function () {
  var POLL_MS = 3000;
  var MAX_PHOTOS = 4; // Same as MessageService.MAX_PHOTOS.

  var form = document.getElementById('composer');
  var input = document.getElementById('msg-images');
  var label = document.getElementById('msg-images-label');
  var text = document.getElementById('msg-text');
  var counter = document.getElementById('msg-count');
  var clearPhotos = document.getElementById('msg-images-clear');

  // Photos picked so far. A browser file box forgets its earlier choice every time you pick again, so this
  // list keeps every photo across picks and is put back into the file box (see the change handler below).
  var picked = [];

  function updatePhotoLabel() {
    if (!input || !label) {
      return;
    }
    var count = input.files.length;
    // "Photo" until something is picked, then "1/4 photos", "2/4 photos"... so it is clear how many more fit.
    label.textContent = count === 0 ? 'Photo' : count + '/' + MAX_PHOTOS + ' photos';
    label.parentNode.title = count === 0
        ? 'Add photos (JPEG or PNG, up to ' + MAX_PHOTOS + ')'
        : count >= MAX_PHOTOS
            ? 'Photo limit reached (' + MAX_PHOTOS + ')'
            : 'Add up to ' + (MAX_PHOTOS - count) + ' more photo' + (MAX_PHOTOS - count === 1 ? '' : 's');
    if (clearPhotos) {
      clearPhotos.hidden = count === 0;
    }
    // At the limit the Photo button is turned off. The file box itself cannot be disabled, because a disabled
    // field is left out when the form is sent (the photos would not go through), so its clicks are blocked.
    var full = count >= MAX_PHOTOS;
    label.parentNode.classList.toggle('attach-full', full);
    label.parentNode.setAttribute('aria-disabled', full ? 'true' : 'false');
  }

  if (input) {
    input.addEventListener('click', function (event) {
      // Covers clicking the Photo button (it is the file box's label) and pressing Enter/Space on the file box.
      if (input.files.length >= MAX_PHOTOS) {
        event.preventDefault();
      }
    });
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

    // Gives an error box an × button that removes it.
    var addDismiss = function (flash) {
      if (flash.querySelector('.flash-close')) {
        return;
      }
      var close = document.createElement('button');
      close.type = 'button';
      close.className = 'flash-close';
      close.setAttribute('aria-label', 'Dismiss this message');
      close.innerHTML = '<svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" '
          + 'stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12"></path></svg>';
      close.addEventListener('click', function () {
        flash.remove();
        if (text) {
          text.focus({ preventScroll: true });
        }
      });
      flash.appendChild(close);
    };

    // Puts an error just above the composer, replacing any earlier one; null just clears it.
    var showFlash = function (flash) {
      var old = document.querySelector('.thread .flash');
      if (old) {
        old.remove();
      }
      if (flash) {
        addDismiss(flash);
        form.parentNode.insertBefore(flash, form);
      }
    };

    // An error the server put on the page (after a send without this script, or the size fallback) can be
    // dismissed too. Drop ?error= from the address so a reload does not bring the message back.
    var pageFlash = document.querySelector('.thread .flash');
    if (pageFlash) {
      addDismiss(pageFlash);
    }
    if (window.history && history.replaceState && /[?&]error=/.test(location.search)) {
      var cleanSearch = location.search.replace(/([?&])error=[^&]*&?/, '$1').replace(/[?&]$/, '');
      history.replaceState(null, '', location.pathname + cleanSearch + location.hash);
    }

    var plainFlash = function (message) {
      var div = document.createElement('div');
      div.className = 'flash flash-bad';
      div.setAttribute('role', 'alert');
      div.textContent = message;
      return div;
    };

    // Same limits as MessageService. Checked here first so a photo that is too big never gets uploaded; the
    // server checks everything again.
    var MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    var PHOTO_TYPES = ['image/jpeg', 'image/png'];

    var photoProblem = function () {
      if (!input) {
        return null;
      }
      var files = Array.prototype.slice.call(input.files);
      if (files.length > MAX_PHOTOS) {
        return 'You can send at most ' + MAX_PHOTOS + ' photos at a time. You picked ' + files.length + '.';
      }
      for (var i = 0; i < files.length; i++) {
        var file = files[i];
        if (file.size > MAX_PHOTO_BYTES) {
          var mb = (file.size / (1024 * 1024)).toFixed(1);
          return '"' + file.name + '" is ' + mb + ' MB. Each photo must be 5 MB or smaller.';
        }
        if (file.type && PHOTO_TYPES.indexOf(file.type) === -1) {
          return '"' + file.name + '" is not a JPEG or PNG image.';
        }
      }
      return null;
    };

    // As soon as photos are picked, drop any that would not go through (and any past the 4th), so the photo
    // count and what gets sent only include usable photos, and say why the others were left out.
    if (input) {
      var canEditFiles = typeof DataTransfer !== 'undefined';

      input.addEventListener('change', function () {
        var problems = [];
        // Photos kept from earlier picks come first, then the new ones. Every pick is added, even one with the
        // same file name as an earlier photo (different photos often share names like "image.jpg").
        var kept = canEditFiles ? picked.slice() : [];
        Array.prototype.forEach.call(input.files, function (file) {
          if (file.size > MAX_PHOTO_BYTES) {
            var mb = (file.size / (1024 * 1024)).toFixed(1);
            problems.push('"' + file.name + '" is ' + mb + ' MB. Each photo must be 5 MB or smaller.');
          } else if (file.type && PHOTO_TYPES.indexOf(file.type) === -1) {
            problems.push('"' + file.name + '" is not a JPEG or PNG image.');
          } else if (kept.length >= MAX_PHOTOS) {
            problems.push('"' + file.name + '" was left out: you can send at most ' + MAX_PHOTOS + ' photos at a time.');
          } else {
            kept.push(file);
          }
        });

        if (canEditFiles) {
          var transfer = new DataTransfer();
          kept.forEach(function (file) {
            transfer.items.add(file);
          });
          input.files = transfer.files;
          picked = kept;
        } else if (problems.length > 0) {
          // Very old browser: the list cannot be edited, so clear it; sending is blocked anyway.
          input.value = '';
        }
        updatePhotoLabel();
        showFlash(problems.length ? plainFlash(problems.join(' ')) : null);
      });

      // Photos now add up across picks, so there has to be a way to take them back out.
      if (clearPhotos) {
        clearPhotos.addEventListener('click', function () {
          input.value = '';
          picked = [];
          updatePhotoLabel();
          showFlash(null);
          input.focus();
        });
      }
    }

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      if (sending) {
        return;
      }
      var problem = photoProblem();
      if (problem) {
        showFlash(plainFlash(problem));
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
          if (response.status === 413) {
            throw new Error('Photos must be 5 MB or smaller each.');
          }
          if (!response.ok) {
            throw new Error('');
          }
          return response.text();
        })
        .then(function (html) {
          var doc = parsePage(html);
          if (!doc || !doc.querySelector('.bubbles')) {
            throw new Error('');
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
            picked = [];
            updatePhotoLabel();
            updateCount();
          }
        })
        .catch(function (error) {
          // Reasons thrown above are shown as is. A TypeError is fetch failing (no connection), shown generally.
          var reason = error && !(error instanceof TypeError) ? error.message : '';
          showFlash(plainFlash(reason || 'The message could not be sent. Refresh the page and try again.'));
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
