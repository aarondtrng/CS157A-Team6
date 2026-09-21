import CountUp from './CountUp.jsx'
import FadeIn from './FadeIn.jsx'
import styles from './Stats.module.css'

const stats = [
  { end: 100, suffix: '+', label: 'Items returned' },
  { end: 30, suffix: '+', label: 'Active posts' },
  { end: 300, suffix: '+', label: 'Students helped' },
  { end: 48, suffix: ' hrs', label: 'Average return time' },
]

function Stats() {
  return (
    <section className={styles.stats} aria-label="SpartanFinds statistics">
      <div className="container">
        <ul className={styles.grid}>
          {stats.map(({ end, suffix, label }, index) => (
            <li key={label}>
              <FadeIn delay={index * 100} className={styles.stat}>
                <span className={styles.value}>
                  <CountUp end={end} suffix={suffix} />
                </span>
                <span className={styles.label}>{label}</span>
              </FadeIn>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

export default Stats
