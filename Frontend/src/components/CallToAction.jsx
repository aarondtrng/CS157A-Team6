import { REPORT_LOST_URL } from '../config.js'
import styles from './CallToAction.module.css'

// "Found" posts open the form with the found option preselected; guests are sent to log in first.
const REPORT_FOUND_URL = REPORT_LOST_URL.replace('type=LOST', 'type=FOUND')

function CallToAction() {
  return (
    <section className={styles.band}>
      <div className={`container ${styles.inner}`}>
        <div className={styles.text}>
          <h2 className={styles.title}>Found something that isn&rsquo;t yours?</h2>
          <p className={styles.body}>Posting takes a minute. Sign in with your SJSU email.</p>
        </div>
        <div className={styles.actions}>
          <a href={REPORT_FOUND_URL} className="btn btn-gold">
            Report a found item
          </a>
          <a href={REPORT_LOST_URL} className="btn btn-ghost">
            Report a lost item
          </a>
        </div>
      </div>
    </section>
  )
}

export default CallToAction
