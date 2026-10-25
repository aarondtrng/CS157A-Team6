import sjsuLogo from '../assets/sjsuLogo.svg'
import { CATALOG_URL, LOGIN_URL, MAP_URL, SIGNUP_URL } from '../config.js'
import styles from './NavBar.module.css'

const navLinks = [
  { label: 'Browse Items', href: CATALOG_URL },
  { label: 'Campus Map', href: MAP_URL },
]

function NavBar() {
  return (
    <header className={styles.navbar}>
      <div className={`container ${styles.inner}`}>
        <a href="#" className={styles.brand}>
          <img src={sjsuLogo} alt="SJSU Spartan logo" className={styles.logo} />
          SpartanFinds
        </a>

        <nav aria-label="Main navigation">
          <ul className={styles.links}>
            {navLinks.map(({ label, href }) => (
              <li key={label}>
                <a href={href} className={styles.link}>
                  {label}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <div className={styles.actions}>
          <a href={LOGIN_URL} className={`btn btn-outline ${styles.navBtn}`}>
            Login
          </a>
          <a href={SIGNUP_URL} className={`btn btn-gold ${styles.navBtn}`}>
            Sign Up
          </a>
        </div>
      </div>
    </header>
  )
}

export default NavBar
