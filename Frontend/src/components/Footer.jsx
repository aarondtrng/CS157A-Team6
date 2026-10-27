import { CATALOG_URL, MAP_URL } from '../config.js'
import styles from './Footer.module.css'

function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={`container ${styles.inner}`}>
        <span>© 2026 SpartanFinds · CS157A Team 6</span>
        <nav aria-label="Footer" className={styles.links}>
          <a href={CATALOG_URL}>Browse</a>
          <a href={MAP_URL}>Campus map</a>
          <a href="#how">How it works</a>
        </nav>
      </div>
    </footer>
  )
}

export default Footer
