import FadeIn from './FadeIn.jsx'
import sjsuLogo from '../assets/sjsuLogo.svg'
import { CATALOG_URL, REPORT_LOST_URL } from '../config.js'
import styles from './Hero.module.css'

function Hero() {
  return (
    <section className={styles.hero}>
      <img src={sjsuLogo} alt="" aria-hidden="true" className={styles.watermark} />
      <div className={`container ${styles.inner}`}>
        <FadeIn className={styles.copy}>
          <h1>
            Help Lost Items{' '}
            <span className={styles.highlight}>Find Their Way Home</span>
          </h1>
          <p className={styles.description}>
            SpartanFinds makes it easy for SJSU students to report
            lost belongings, post items they&apos;ve found, and get them back
            to their owners.
          </p>
          <div className={styles.actions}>
            <a href={REPORT_LOST_URL} className="btn btn-gold">
              Report a Lost Item
            </a>
            <a href={CATALOG_URL} className={`btn btn-outline ${styles.outlineBtn}`}>
              Browse Found Items
            </a>
          </div>
        </FadeIn>
      </div>
    </section>
  )
}

export default Hero
