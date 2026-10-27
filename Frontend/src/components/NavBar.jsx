import sjsuLogo from '../assets/sjsuLogo.svg'
import { CATALOG_URL, LOGIN_URL, LOGOUT_URL, MAP_URL, REPORT_LOST_URL, SIGNUP_URL, profileUrl } from '../config.js'
import { getLoggedInFirstName, getLoggedInUserId } from '../session.js'
import styles from './NavBar.module.css'

const navLinks = [
  { label: 'Browse', href: CATALOG_URL },
  { label: 'Campus map', href: MAP_URL },
]

function NavBar() {
  const firstName = getLoggedInFirstName()
  const userId = getLoggedInUserId()

  return (
    <header className={styles.navbar}>
      <nav aria-label="Main" className={`container ${styles.inner}`}>
        <a href="#" className={styles.brand}>
          <img src={sjsuLogo} alt="SJSU Spartan logo" className={styles.logo} />
          SpartanFinds
        </a>

        <ul className={styles.links}>
          {navLinks.map(({ label, href }) => (
            <li key={label}>
              <a href={href} className={styles.link}>
                {label}
              </a>
            </li>
          ))}
        </ul>

        <div className={styles.actions}>
          {firstName ? (
            <>
              <a href={REPORT_LOST_URL} className={`btn ${styles.navBtn}`}>
                + Create post
              </a>
              {userId ? (
                <a href={profileUrl(userId)} className={styles.userName} title="View your profile">
                  Hi, {firstName}
                </a>
              ) : (
                <span className={styles.userName}>Hi, {firstName}</span>
              )}
              <a href={LOGOUT_URL} className={`btn btn-outline ${styles.navBtn}`}>
                Log out
              </a>
            </>
          ) : (
            <>
              <a href={LOGIN_URL} className={`btn btn-outline ${styles.navBtn}`}>
                Log in
              </a>
              <a href={SIGNUP_URL} className={`btn ${styles.navBtn}`}>
                Sign up
              </a>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}

export default NavBar
