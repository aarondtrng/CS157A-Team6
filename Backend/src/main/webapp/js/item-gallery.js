// Item page photo gallery: the small photo buttons scroll the big gallery to that photo, and the pressed
// button follows the gallery when you scroll it sideways. The gallery works without this script (it scrolls).
(function () {
  'use strict';

  var gallery = document.querySelector('.gallery');
  var thumbs = document.querySelectorAll('.gallery-thumb');
  if (!gallery || thumbs.length === 0) {
    return;
  }

  function mark(index) {
    thumbs.forEach(function (thumb, i) {
      thumb.setAttribute('aria-pressed', i === index ? 'true' : 'false');
    });
  }

  thumbs.forEach(function (thumb, index) {
    thumb.addEventListener('click', function () {
      gallery.scrollTo({ left: gallery.clientWidth * index, behavior: 'smooth' });
      mark(index);
    });
  });

  gallery.addEventListener('scroll', function () {
    if (gallery.clientWidth > 0) {
      mark(Math.round(gallery.scrollLeft / gallery.clientWidth));
    }
  });
})();
