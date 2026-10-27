// Who is logged in, as far as the landing page can tell. The backend (Tomcat, another port) sets a small
// "sf_user" cookie with the user's display name when someone logs in and removes it at logout. Browsers
// share cookies across ports on one host, so this page can read it. It is only a display hint: the
// real login lives in the backend's session, which this page cannot see and does not need.
const COOKIE_NAME = 'sf_user'

function readCookie(name) {
  const entry = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${name}=`))
  return entry ? entry.slice(name.length + 1) : null
}

// The first word of the logged-in user's name, or null when nobody is logged in.
export function getLoggedInFirstName() {
  const raw = readCookie(COOKIE_NAME)
  if (!raw) {
    return null
  }
  try {
    const name = decodeURIComponent(raw).trim()
    return name ? name.split(' ')[0] : null
  } catch {
    return null
  }
}

// The logged-in user's id (digits only), or null. The "sf_uid" cookie is set next to "sf_user" at login.
export function getLoggedInUserId() {
  const raw = readCookie('sf_uid')
  return raw && /^[0-9]{1,10}$/.test(raw) ? raw : null
}
