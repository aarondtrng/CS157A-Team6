// Header account menu: closes the menu when you click outside it or press Escape. The menu itself is a
// native <details> element, so it opens and closes with no script; this only adds the usual extras.
(function () {
  'use strict';

  var menu = document.querySelector('.user-menu');
  if (!menu) {
    return;
  }

  document.addEventListener('click', function (event) {
    if (menu.open && !menu.contains(event.target)) {
      menu.open = false;
    }
  });

  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape' && menu.open) {
      menu.open = false;
      var summary = menu.querySelector('summary');
      if (summary) {
        summary.focus();
      }
    }
  });
})();
