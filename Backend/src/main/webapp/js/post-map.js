// Location picker for the "post an item" form: a Leaflet map with OpenStreetMap tiles. Clicking drops a
// pin and copies its latitude/longitude into the form's hidden fields. The server re-checks the values
// (service.PostService), so this script only makes choosing a spot convenient.
(function () {
  'use strict';

  var mapElement = document.getElementById('location-map');
  if (!mapElement || typeof L === 'undefined') {
    return;
  }

  var latInput = document.getElementById('latitude');
  var lngInput = document.getElementById('longitude');
  var chosenText = document.getElementById('location-chosen');
  var clearButton = document.getElementById('location-clear');

  // Same campus area the server accepts (PostService.MIN_LATITUDE ... MAX_LONGITUDE).
  var campus = L.latLngBounds([37.325, -121.895], [37.345, -121.870]);
  var map = L.map(mapElement, {
    center: [37.3352, -121.8811],
    zoom: 16,
    minZoom: 14,
    maxBounds: campus.pad(0.5),
    maxBoundsViscosity: 0.8
  });

  L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map);

  var marker = null;

  function showPin(latlng) {
    if (marker) {
      marker.setLatLng(latlng);
    } else {
      marker = L.marker(latlng).addTo(map);
    }
    latInput.value = latlng.lat.toFixed(5);
    lngInput.value = latlng.lng.toFixed(5);
    chosenText.textContent = 'Spot chosen. Click the map again to move the pin.';
    clearButton.hidden = false;
  }

  function clearPin() {
    if (marker) {
      map.removeLayer(marker);
      marker = null;
    }
    latInput.value = '';
    lngInput.value = '';
    chosenText.textContent = 'No spot chosen yet.';
    clearButton.hidden = true;
  }

  map.on('click', function (event) {
    if (!campus.contains(event.latlng)) {
      chosenText.textContent = 'Please choose a spot on or near the SJSU campus.';
      return;
    }
    showPin(event.latlng);
  });

  clearButton.addEventListener('click', clearPin);

  // Show the pin again when the form comes back with an error.
  var startLat = parseFloat(mapElement.getAttribute('data-lat'));
  var startLng = parseFloat(mapElement.getAttribute('data-lng'));
  if (isFinite(startLat) && isFinite(startLng)) {
    showPin(L.latLng(startLat, startLng));
    map.setView([startLat, startLng], 17);
  }
})();
