// Phone number box on the edit-profile form: formats what the student types as (408) 555-0123, accepts
// pasted numbers like +1 408 555 0123, and stops at 10 digits. The server checks the same format again.
(function () {
  'use strict';

  var input = document.getElementById('phoneNumber');
  if (!input) {
    return;
  }

  function format(text) {
    var digits = text.replace(/\D/g, '');
    if (digits.length > 10 && digits.charAt(0) === '1') {
      digits = digits.slice(1);
    }
    digits = digits.slice(0, 10);
    if (digits.length === 0) {
      return '';
    }
    if (digits.length <= 3) {
      return '(' + digits;
    }
    if (digits.length <= 6) {
      return '(' + digits.slice(0, 3) + ') ' + digits.slice(3);
    }
    return '(' + digits.slice(0, 3) + ') ' + digits.slice(3, 6) + '-' + digits.slice(6);
  }

  input.addEventListener('input', function () {
    var formatted = format(input.value);
    if (formatted !== input.value) {
      input.value = formatted;
    }
  });

  // A number saved before this format existed is shown as is; typing or pasting replaces it.
})();
