// Shows how many photos are picked in the message composer. The form works without this script.
(function () {
  var input = document.getElementById('msg-images');
  var label = document.getElementById('msg-images-label');
  if (!input || !label) {
    return;
  }
  input.addEventListener('change', function () {
    var count = input.files.length;
    label.textContent = count === 0 ? 'Photo' : count === 1 ? '1 photo' : count + ' photos';
  });
})();
