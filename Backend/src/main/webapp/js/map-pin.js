// Pin icon shared by the location picker and the campus map: a teardrop in the app's colors (gold for LOST,
// blue for everything else), matching the design boards. The markup is a fixed string with no item text.
(function () {
  'use strict';

  window.sfPinIcon = function (kind) {
    var lost = kind === 'LOST';
    var fill = lost ? '#e5a823' : '#0055a2';
    var dot = lost ? '#0055a2' : '#e5a823';
    return L.divIcon({
      className: 'sf-pin',
      html: '<svg width="34" height="44" viewBox="0 0 40 52" aria-hidden="true">'
        + '<path d="M20 50C20 50 36 33 36 19A16 16 0 0 0 4 19C4 33 20 50 20 50Z" fill="' + fill + '"/>'
        + '<circle cx="20" cy="19" r="6" fill="' + dot + '"/></svg>',
      iconSize: [34, 44],
      iconAnchor: [17, 44],
      popupAnchor: [0, -40]
    });
  };
})();
