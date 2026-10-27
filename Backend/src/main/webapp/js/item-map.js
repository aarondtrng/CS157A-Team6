// Small read-only map on the item page: shows a pin where the poster lost or found the item. The position
// comes from data-lat / data-lng on the map element, so nothing is fetched from the server.
(function () {
  'use strict';

  var mapElement = document.getElementById('item-map');
  if (!mapElement || typeof L === 'undefined') {
    return;
  }

  var lat = parseFloat(mapElement.getAttribute('data-lat'));
  var lng = parseFloat(mapElement.getAttribute('data-lng'));
  if (!isFinite(lat) || !isFinite(lng)) {
    return;
  }

  var map = L.map(mapElement, { center: [lat, lng], zoom: 17, minZoom: 13, scrollWheelZoom: false });

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  L.marker([lat, lng], { icon: window.sfPinIcon(mapElement.getAttribute('data-type')), keyboard: false }).addTo(map);

  // Re-measure if the layout shifts after Leaflet starts (fonts loading, a scrollbar appearing).
  if (window.ResizeObserver) {
    new ResizeObserver(function () {
      map.invalidateSize();
    }).observe(mapElement);
  }
})();
