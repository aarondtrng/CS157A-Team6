import FadeIn from './FadeIn.jsx'
import styles from './CallToAction.module.css'

function CallToAction() {
  return (
    <section className={styles.cta}>
      <div className="container">
        <FadeIn className={styles.inner}>
          <h2>Lost something or found an item?</h2>
          <p className={styles.subtext}>
            Create a post in minutes and reach students across campus.
          </p>
          <button type="button" className="btn btn-gold">
            Get Started
          </button>
        </FadeIn>
      </div>
    </section>
  )
}

export default CallToAction
