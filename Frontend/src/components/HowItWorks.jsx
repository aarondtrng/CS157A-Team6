import styles from './HowItWorks.module.css'

const steps = [
  { n: '01', title: 'Post what you lost or found', body: 'Add a title, photos and a category, then drop a pin where it happened.' },
  { n: '02', title: 'Search and match', body: 'Filter by category, sort by date, or browse pins on the campus map.' },
  { n: '03', title: 'Claim it privately', body: 'Send ownership details only the finder sees, then arrange the return.' },
]

function HowItWorks() {
  return (
    <section id="how" className={`container ${styles.section}`}>
      <h2 className="section-title">How it works</h2>
      <ol className={styles.steps}>
        {steps.map((step) => (
          <li key={step.n} className={styles.step}>
            <span className={styles.number}>{step.n}</span>
            <h3 className={styles.title}>{step.title}</h3>
            <p className={styles.body}>{step.body}</p>
          </li>
        ))}
      </ol>
    </section>
  )
}

export default HowItWorks
