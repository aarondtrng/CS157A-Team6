// Where the Tomcat backend lives. Override with VITE_BACKEND_URL (e.g. in an untracked .env.local).
const backendUrl = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080/spartanfinds'

export const CATALOG_URL = `${backendUrl}/items`
export const LOGIN_URL = `${backendUrl}/login`
export const SIGNUP_URL = `${backendUrl}/signup`
