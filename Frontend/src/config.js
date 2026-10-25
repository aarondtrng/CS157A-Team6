// Where the Tomcat backend lives. Override with VITE_BACKEND_URL (e.g. in an untracked .env.local).
const backendUrl = import.meta.env.VITE_BACKEND_URL ?? 'http://localhost:8080/spartanfinds'

// TEMPORARY dev link: demo=1 makes the backend show fake catalog items (local machine only).
// Remove "?demo=1" once the ItemDao SQL is written. See Backend/src/main/java/demo/MockItemDao.java.
export const CATALOG_URL = `${backendUrl}/items?demo=1`
