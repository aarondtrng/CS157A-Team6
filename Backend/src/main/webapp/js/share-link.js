// Copy the canonical post URL; selecting the link also works when Clipboard access is unavailable.
const shareControls = document.querySelectorAll('.share-control');
shareControls.forEach((control) => {
  const input = control.querySelector('input');
  const button = control.querySelector('[data-copy-link]');
  const status = control.querySelector('.share-status');
  input.value = new URL(control.dataset.shareUrl, window.location.href).href;
  button.hidden = false;
  input.addEventListener('click', () => input.select());
  const panel = control.querySelector('.share-panel');
  const positionPanel = () => {
    if (!control.open) return;
    panel.style.setProperty('--share-offset', '0px');
    const bounds = panel.getBoundingClientRect();
    const offset = bounds.left < 16 ? 16 - bounds.left
      : Math.min(0, document.documentElement.clientWidth - 16 - bounds.right);
    panel.style.setProperty('--share-offset', `${offset}px`);
  };
  control.addEventListener('toggle', () => {
    if (!control.open) return;
    shareControls.forEach((other) => { if (other !== control) other.open = false; });
    positionPanel();
  });
  window.addEventListener('resize', positionPanel);
  button.addEventListener('click', async () => {
    try {
      if (!navigator.clipboard?.writeText) throw new Error('Clipboard unavailable');
      await navigator.clipboard.writeText(input.value);
      status.textContent = 'Link copied.';
    } catch {
      input.focus();
      input.select();
      status.textContent = 'Select and copy the link above.';
    }
  });
});

document.addEventListener('click', (event) => {
  shareControls.forEach((control) => {
    if (!control.contains(event.target)) control.open = false;
  });
});

document.addEventListener('keydown', (event) => {
  if (event.key !== 'Escape') return;
  shareControls.forEach((control) => {
    if (control.open) {
      control.open = false;
      control.querySelector('summary').focus();
    }
  });
});
