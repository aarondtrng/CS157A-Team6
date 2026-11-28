// Profile picture chooser on the edit-profile form: shows the chosen file in the round preview before saving,
// and unticks "Remove my current picture" when a new one is chosen (a new picture wins on the server too).
// A file that is too big or not a JPEG/PNG is cleared right away with a red warning that has an × to dismiss
// it, so it is never uploaded. The server checks the file type and size again.
(function () {
  'use strict';

  var input = document.getElementById('profileImage');
  var preview = document.getElementById('avatar-preview');
  if (!input || !preview) {
    return;
  }
  var remove = document.getElementById('removeImage');
  var fileName = document.getElementById('file-name');
  var original = preview.innerHTML;
  var objectUrl = null;

  // Same limits as UserProfileService.
  var MAX_IMAGE_BYTES = 5 * 1024 * 1024;
  var IMAGE_TYPES = ['image/jpeg', 'image/png'];

  // Gives a warning box an × button that removes it.
  function addDismiss(flash) {
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
      input.focus({ preventScroll: true });
    });
    flash.appendChild(close);
  }

  function clearFlash() {
    var old = input.parentNode.querySelector('.avatar-flash');
    if (old) {
      old.remove();
    }
  }

  // Shows a warning under the buttons, replacing any earlier one.
  function showFlash(message) {
    clearFlash();
    var flash = document.createElement('div');
    flash.className = 'flash flash-bad avatar-flash';
    flash.setAttribute('role', 'alert');
    flash.textContent = message;
    addDismiss(flash);
    // Above the "JPEG or PNG, up to 5 MB" note, which stays at the very bottom.
    input.parentNode.insertBefore(flash, document.getElementById('image-hint'));
  }

  // The text beside the "choose" button: the picked file's name, or "No file chosen".
  function showFileName() {
    if (!fileName) {
      return;
    }
    var file = input.files && input.files[0];
    fileName.textContent = file ? file.name : 'No file chosen';
    fileName.classList.toggle('has-file', !!file);
  }

  function resetPreview() {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl);
      objectUrl = null;
    }
    preview.innerHTML = original;
  }

  // A warning the server put on the page can be dismissed too.
  var pageFlash = input.parentNode.querySelector('.avatar-flash');
  if (pageFlash) {
    addDismiss(pageFlash);
  }

  input.addEventListener('change', function () {
    handleChange();
    showFileName();
  });

  function handleChange() {
    resetPreview();
    var file = input.files && input.files[0];
    if (!file) {
      return;
    }
    if (IMAGE_TYPES.indexOf(file.type) === -1) {
      input.value = '';
      showFlash('Profile picture must be a JPEG or PNG image.');
      return;
    }
    if (file.size > MAX_IMAGE_BYTES) {
      input.value = '';
      showFlash('That picture is too big. Profile pictures must be 5 MB or smaller.');
      return;
    }
    clearFlash();
    if (remove) {
      remove.checked = false;
      preview.classList.remove('is-removing');
    }
    if (window.URL && URL.createObjectURL) {
      objectUrl = URL.createObjectURL(file);
      var image = document.createElement('img');
      image.src = objectUrl;
      image.alt = 'Preview of your new profile picture';
      preview.replaceChildren(image);
    }
  }

  // Removal selected: the preview shows the initial the profile will fall back to; undo brings the picture back.
  if (remove) {
    remove.addEventListener('change', function () {
      if (remove.checked) {
        if (input.value) {
          input.value = '';
          resetPreview();
          showFileName();
        }
        clearFlash();
        preview.textContent = preview.getAttribute('data-initial') || '';
        preview.classList.add('is-removing');
      } else {
        preview.classList.remove('is-removing');
        resetPreview();
      }
    });
  }
})();
