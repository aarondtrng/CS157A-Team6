// Message composer helpers: shows how many photos are picked and a live character count.
// The form works without this script.
(function () {
  var input = document.getElementById('msg-images');
  var label = document.getElementById('msg-images-label');
  if (input && label) {
    input.addEventListener('change', function () {
      var count = input.files.length;
      label.textContent = count === 0 ? 'Photo' : count === 1 ? '1 photo' : count + ' photos';
    });
  }

  var text = document.getElementById('msg-text');
  var counter = document.getElementById('msg-count');
  if (text && counter) {
    var max = text.maxLength > 0 ? text.maxLength : 5000;
    var warnAt = max - 200;
    var update = function () {
      var length = text.value.length;
      counter.textContent = length.toLocaleString() + ' / ' + max.toLocaleString();
      counter.classList.toggle('near-limit', length >= warnAt);
    };
    text.addEventListener('input', update);
    update();
    counter.hidden = false;
  }
})();
