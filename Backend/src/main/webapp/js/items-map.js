// Campus map: puts a pin on the map for every item in the server-rendered list (#map-items). Each list
// entry carries data-lat / data-lng, and its link and text are read from the entry itself, so nothing is
// fetched from the server. Popups are built with DOM calls (no innerHTML), so item text can never run as
// markup.
(function () {
  'use strict';

  var mapElement = document.getElementById('items-map');
  if (!mapElement || typeof L === 'undefined') {
    return;
  }

  var map = L.map(mapElement, { center: [37.3352, -121.8811], zoom: 16, minZoom: 13 });

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  var entries = document.querySelectorAll('#map-items li');
  var points = [];

  entries.forEach(function (entry) {
    var lat = parseFloat(entry.getAttribute('data-lat'));
    var lng = parseFloat(entry.getAttribute('data-lng'));
    var link = entry.querySelector('a');
    if (!isFinite(lat) || !isFinite(lng) || !link) {
      return;
    }

    var popup = document.createElement('div');
    var type = document.createElement('strong');
    type.textContent = entry.getAttribute('data-type') + ': ';
    var title = document.createElement('a');
    title.href = link.getAttribute('href');
    title.textContent = link.textContent;
    popup.appendChild(type);
    popup.appendChild(title);

    L.marker([lat, lng]).addTo(map).bindPopup(popup);
    points.push([lat, lng]);
  });

  if (points.length > 1) {
    map.fitBounds(points, { padding: [40, 40], maxZoom: 17 });
  }

  // Leaflet measures its box once when it starts. If the page layout then shifts (a scrollbar appears,
  // fonts load), its idea of the size goes stale and the map looks off-center. Re-measure on any change.
  if (window.ResizeObserver) {
    new ResizeObserver(function () {
      map.invalidateSize();
    }).observe(mapElement);
  }
})();
