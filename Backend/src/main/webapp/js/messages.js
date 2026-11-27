// Message composer helpers: shows how many photos are picked, a live character count, and sends messages
// without reloading the page. The form still works without this script (it posts and redirects back).
(function () {
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

  if (!form || !window.fetch || !window.FormData || !window.DOMParser) {
    return;
  }

  var button = form.querySelector('button[type="submit"]');
  var sending = false;

  // Puts the server's error message (or our own) just above the composer, replacing any earlier one.
  function showFlash(flash) {
    var old = document.querySelector('.thread .flash');
    if (old) {
      old.remove();
    }
    if (flash) {
      form.parentNode.insertBefore(flash, form);
    }
  }

  function plainFlash(message) {
    var div = document.createElement('div');
    div.className = 'flash flash-bad';
    div.setAttribute('role', 'alert');
    div.textContent = message;
    return div;
  }

  // Swaps the element matching selector in the live page for the one in the freshly rendered page.
  function swap(doc, selector) {
    var fresh = doc.querySelector(selector);
    var current = document.querySelector(selector);
    if (fresh && current) {
      current.replaceWith(document.importNode(fresh, true));
    }
  }

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    if (sending) {
      return;
    }
    sending = true;
    if (button) {
      button.disabled = true;
    }

    // The server answers with a redirect back to the conversation; fetch follows it and gets that page's HTML.
    fetch(form.action, { method: 'POST', body: new FormData(form), credentials: 'same-origin' })
      .then(function (response) {
        if (!response.ok) {
          throw new Error('HTTP ' + response.status);
        }
        return response.text();
      })
      .then(function (html) {
        var doc = new DOMParser().parseFromString(html, 'text/html');
        if (!doc.querySelector('.bubbles')) {
          // Not the conversation page (for example the login page after the session ran out).
          throw new Error('Unexpected page');
        }
        var flash = doc.querySelector('.thread .flash');

        // Keep the composer at the same spot on screen while the message list above it grows.
        var before = form.getBoundingClientRect().top;
        swap(doc, '.bubbles');
        swap(doc, '.convo-list ul');
        showFlash(flash ? document.importNode(flash, true) : null);
        window.scrollBy(0, form.getBoundingClientRect().top - before);

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
})();
