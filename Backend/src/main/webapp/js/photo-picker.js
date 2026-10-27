// Photo chooser on the "new post" form. A browser file box forgets its earlier choice every time you pick
// again, so photos chosen in two rounds ended up as just the last one. This script keeps every chosen photo,
// shows a thumbnail for each (the first is the cover), lets you remove one, and puts the full list back into
// the file box, so the form still submits the photos the ordinary way. The server checks them again.
(function () {
  'use strict';

  var input = document.getElementById('images');
  var list = document.getElementById('photo-previews');
  var note = document.getElementById('photo-note');
  if (!input || !list || typeof DataTransfer === 'undefined') {
    return;
  }

  var MAX_PHOTOS = parseInt(input.getAttribute('data-max'), 10) || 5;
  var MAX_BYTES = 5 * 1024 * 1024;
  var files = [];
  var previewUrls = [];

  function keyOf(file) {
    return file.name + '|' + file.size + '|' + file.lastModified;
  }

  function showNote(text) {
    note.textContent = text;
    note.hidden = !text;
  }

  // Puts the kept photos back into the file box so the form submits all of them.
  function syncInput() {
    var transfer = new DataTransfer();
    files.forEach(function (file) {
      transfer.items.add(file);
    });
    input.files = transfer.files;
  }

  function render() {
    previewUrls.forEach(function (url) {
      URL.revokeObjectURL(url);
    });
    previewUrls = [];
    list.textContent = '';

    files.forEach(function (file, index) {
      var url = URL.createObjectURL(file);
      previewUrls.push(url);

      var item = document.createElement('li');
      var image = document.createElement('img');
      image.src = url;
      image.alt = 'Photo ' + (index + 1) + ': ' + file.name;
      item.appendChild(image);

      if (index === 0) {
        var cover = document.createElement('span');
        cover.className = 'photo-cover';
        cover.textContent = 'COVER';
        item.appendChild(cover);
      }

      var remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'photo-remove';
      remove.setAttribute('aria-label', 'Remove photo ' + (index + 1));
      remove.textContent = '×';
      remove.addEventListener('click', function () {
        files.splice(index, 1);
        showNote('');
        syncInput();
        render();
      });
      item.appendChild(remove);

      list.appendChild(item);
    });
  }

  input.addEventListener('change', function () {
    var problems = [];
    Array.prototype.forEach.call(input.files, function (file) {
      if (files.some(function (kept) { return keyOf(kept) === keyOf(file); })) {
        return; // already chosen
      }
      if (file.type !== 'image/jpeg' && file.type !== 'image/png') {
        problems.push(file.name + ' is not a JPEG or PNG image.');
      } else if (file.size > MAX_BYTES) {
        problems.push(file.name + ' is larger than 5 MB.');
      } else if (files.length >= MAX_PHOTOS) {
        problems.push('You can add at most ' + MAX_PHOTOS + ' photos.');
      } else {
        files.push(file);
      }
    });
    showNote(problems.join(' '));
    syncInput();
    render();
  });
})();
