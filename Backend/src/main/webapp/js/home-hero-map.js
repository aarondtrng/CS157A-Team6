// Landing page hero: a real map of the SJSU campus as the background, with pins for the newest posts. The
// pins come from the hidden list #hero-pins (data-lat / data-lng / data-type), so nothing is fetched from the
// server. The map is only decoration: it cannot be moved, zoomed, or focused.
(function () {
  'use strict';

  var mapElement = document.getElementById('hero-map');
  if (!mapElement || typeof L === 'undefined') {
    return;
  }

  // The campus, as a box. On a wide screen the map is zoomed in (level 17) so the campus fills most of the
  // hero, with the campus in the middle; on a narrow screen it is zoomed out just enough to fit the box.
  var campus = L.latLngBounds([37.3315, -121.8870], [37.3375, -121.8760]);

  function showCampus() {
    if (mapElement.clientWidth >= 900) {
      map.setView(campus.getCenter(), 17, { animate: false });
    } else {
      map.fitBounds(campus, { animate: false });
    }
  }

  var map = L.map(mapElement, {
    center: campus.getCenter(),
    zoom: 17,
    zoomControl: false,
    dragging: false,
    scrollWheelZoom: false,
    doubleClickZoom: false,
    boxZoom: false,
    keyboard: false,
    touchZoom: false
  });

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  showCampus();

  document.querySelectorAll('#hero-pins li').forEach(function (entry, index) {
    var lat = parseFloat(entry.getAttribute('data-lat'));
    var lng = parseFloat(entry.getAttribute('data-lng'));
    if (!isFinite(lat) || !isFinite(lng)) {
      return;
    }
    var marker = L.marker([lat, lng], {
      icon: window.sfPinIcon(entry.getAttribute('data-type')),
      interactive: false,
      keyboard: false
    }).addTo(map);
    var element = marker.getElement();
    if (element) {
      element.style.setProperty('--delay', (0.4 + index * 0.12).toFixed(2) + 's');
    }
  });

  // Re-measure if the layout shifts after Leaflet starts (fonts loading, a scrollbar appearing).
  if (window.ResizeObserver) {
    new ResizeObserver(function () {
      map.invalidateSize();
      showCampus();
    }).observe(mapElement);
  }
})();
