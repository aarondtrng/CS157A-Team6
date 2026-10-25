// Where the Tomcat backend lives. Override with VITE_BACKEND_URL (e.g. in an untracked .env.local).
const backendUrl = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080/spartanfinds'

export const CATALOG_URL = `${backendUrl}/items`
export const MAP_URL = `${backendUrl}/map`
// Opens the post form with "Lost" preselected. Guests are sent to log in first and then brought back here.
export const REPORT_LOST_URL = `${backendUrl}/post/new?type=LOST`
export const LOGIN_URL = `${backendUrl}/login`
export const SIGNUP_URL = `${backendUrl}/signup`
