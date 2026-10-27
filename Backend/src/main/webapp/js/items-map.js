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

    var type = entry.getAttribute('data-type');
    var metaText = entry.querySelector('.map-item-meta');
    var titleText = entry.querySelector('.map-item-link');

    // The popup copies text from the list entry with DOM calls (no innerHTML).
    var popup = document.createElement('div');
    popup.className = 'map-popup';

    // The post's first photo, if it has one. Only our own uploads (a path on this site) or https images are
    // used, and the address is set on an <img> element, never written into markup.
    var photoUrl = entry.getAttribute('data-photo');
    if (photoUrl && (photoUrl.charAt(0) === '/' || photoUrl.indexOf('https://') === 0)) {
      var photo = document.createElement('img');
      photo.className = 'map-popup-photo';
      photo.alt = '';
      photo.src = photoUrl;
      popup.appendChild(photo);
    }
    var badge = document.createElement('span');
    badge.className = 'badge-inline badge-' + (type === 'FOUND' ? 'found' : 'lost');
    badge.textContent = type;
    var title = document.createElement('strong');
    title.textContent = titleText ? titleText.textContent : link.textContent;
    var meta = document.createElement('span');
    meta.className = 'map-popup-meta';
    meta.textContent = metaText ? metaText.textContent.replace(/\s+/g, ' ').trim() : '';
    var open = document.createElement('a');
    open.href = link.getAttribute('href');
    open.textContent = 'View post \u2192';
    popup.appendChild(badge);
    popup.appendChild(title);
    popup.appendChild(meta);
    popup.appendChild(open);

    L.marker([lat, lng], { icon: window.sfPinIcon(type) }).addTo(map).bindPopup(popup);
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
