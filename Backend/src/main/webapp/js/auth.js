// Client-side password hashing for the login and sign-up forms (design doc: "hashed on the client side").
//
// The typed password never leaves the browser. Password inputs have no name attribute, so they are not
// submitted; instead this script derives PBKDF2-SHA256(password, salt = app prefix + email) and puts the
// 64-character hex result in the hidden "password" field. The server hashes that value again with its own
// random salt (util.PasswordHasher), so a stolen database still cannot be used to log in.
//
// The email is the salt, so the email must be normalized exactly like AuthService.normalizeEmail
// (trim + lower-case). Changing these constants invalidates every existing account.
(function () {
  'use strict';

  var SALT_PREFIX = 'spartanfinds:';
  var ITERATIONS = 100000;
  var MIN_PASSWORD_LENGTH = 8;
  var MAX_PASSWORD_LENGTH = 128;

  function toHex(buffer) {
    return Array.prototype.map.call(new Uint8Array(buffer), function (b) {
      return b.toString(16).padStart(2, '0');
    }).join('');
  }

  function clientHash(email, password) {
    var encoder = new TextEncoder();
    return crypto.subtle.importKey('raw', encoder.encode(password), 'PBKDF2', false, ['deriveBits'])
      .then(function (key) {
        return crypto.subtle.deriveBits(
          { name: 'PBKDF2', hash: 'SHA-256', salt: encoder.encode(SALT_PREFIX + email), iterations: ITERATIONS },
          key, 256);
      })
      .then(toHex);
  }

  function showError(form, message) {
    var box = form.querySelector('[data-client-error]');
    box.textContent = message;
    box.hidden = false;
  }

  document.querySelectorAll('form[data-hash-password]').forEach(function (form) {
    var email = form.querySelector('input[type="email"]');
    var password = form.querySelector('[data-password]');
    var confirm = form.querySelector('[data-password-confirm]');
    var hidden = form.querySelector('input[name="password"]');
    var submit = form.querySelector('button[type="submit"]');

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      if (!form.reportValidity()) {
        return;
      }
      if (!window.crypto || !crypto.subtle) {
        // Web Crypto only works on HTTPS or localhost.
        showError(form, 'This page must be opened over HTTPS (or on localhost) to log in securely.');
        return;
      }
      if (confirm) {
        if (password.value.length < MIN_PASSWORD_LENGTH || password.value.length > MAX_PASSWORD_LENGTH) {
          showError(form, 'Password must be ' + MIN_PASSWORD_LENGTH + ' to ' + MAX_PASSWORD_LENGTH + ' characters.');
          return;
        }
        if (password.value !== confirm.value) {
          showError(form, 'Passwords do not match.');
          return;
        }
      }

      submit.disabled = true;
      clientHash(email.value.trim().toLowerCase(), password.value)
        .then(function (hex) {
          hidden.value = hex;
          form.submit();
        })
        .catch(function () {
          submit.disabled = false;
          showError(form, 'Could not process your password. Please try again.');
        });
    });
  });
})();
