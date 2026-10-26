// Who is logged in, as far as the landing page can tell. The backend (Tomcat, another port) sets a small
// "sf_user" cookie with the user's display name when someone logs in and removes it at logout. Browsers
// share cookies across ports on one host, so this page can read it. It is only a display hint: the
// real login lives in the backend's session, which this page cannot see and does not need.
const COOKIE_NAME = 'sf_user'

// The first word of the logged-in user's name, or null when nobody is logged in.
export function getLoggedInFirstName() {
  const entry = document.cookie.split('; ').find((cookie) => cookie.startsWith(`${COOKIE_NAME}=`))
  if (!entry) {
    return null
  }
  try {
    const name = decodeURIComponent(entry.slice(COOKIE_NAME.length + 1)).trim()
    return name ? name.split(' ')[0] : null
  } catch {
    return null
  }
}
