// Catalog sort dropdown: reloads the list as soon as a sort option is chosen. The form is an ordinary GET
// form (it keeps the search and category), so this only saves a click; without JavaScript the form's own
// Apply button does the same.
(function () {
  'use strict';

  var select = document.getElementById('sort-select');
  if (!select || !select.form) {
    return;
  }

  select.addEventListener('change', function () {
    select.form.submit();
  });
})();
