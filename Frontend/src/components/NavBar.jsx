import sjsuLogo from '../assets/sjsuLogo.svg'
import styles from './NavBar.module.css'

const navLinks = ['Home', 'Browse Items', 'Campus Map']

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
            {navLinks.map((label) => (
              <li key={label}>
                <a href="#" className={styles.link}>
                  {label}
                </a>
              </li>
            ))}
          </ul>
        </nav>

        <div className={styles.actions}>
          <button type="button" className={`btn btn-outline ${styles.navBtn}`}>
            Login
          </button>
          <button type="button" className={`btn btn-gold ${styles.navBtn}`}>
            Sign Up
          </button>
        </div>
      </div>
    </header>
  )
}

export default NavBar
