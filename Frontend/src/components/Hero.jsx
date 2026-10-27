import { CATALOG_URL } from '../config.js'
import styles from './Hero.module.css'

// Decorative map pins that drop in behind the headline (same colors as the campus map).
const heroPins = [
  { x: '9%', y: '20%', gold: false }, { x: '14%', y: '64%', gold: true },
  { x: '22%', y: '36%', gold: true }, { x: '35%', y: '18%', gold: false },
  { x: '38%', y: '82%', gold: false }, { x: '63%', y: '78%', gold: true },
  { x: '66%', y: '16%', gold: true }, { x: '79%', y: '40%', gold: false },
  { x: '88%', y: '22%', gold: true }, { x: '90%', y: '70%', gold: false },
]

const popular = ['Electronics', 'ID & cards', 'Keys', 'Bags', 'Water bottles']

function Hero() {
  return (
    <section className={styles.hero}>
      <div className={styles.map} aria-hidden="true">
        {heroPins.map((pin, i) => (
          <span
            key={i}
            className={styles.pinWrap}
            style={{ left: pin.x, top: pin.y, '--delay': `${(0.6 + i * 0.14).toFixed(2)}s` }}
          >
            <span className={styles.ring} />
            <svg className={styles.pin} width="28" height="36" viewBox="0 0 28 36">
              <path
                d="M14 35S2 22 2 13a12 12 0 0 1 24 0c0 9-12 22-12 22z"
                fill={pin.gold ? '#e5a823' : '#0055a2'}
                stroke="#faf8f3"
                strokeWidth="2.5"
              />
              <circle cx="14" cy="13" r="4.5" fill={pin.gold ? '#0055a2' : '#e5a823'} />
            </svg>
          </span>
        ))}
      </div>
      <div className={styles.scrim} aria-hidden="true" />

      <div className={styles.content}>
        <p className={styles.eyebrow}>
          <span className={styles.dot} />
          San José State lost &amp; found
        </p>
        <h1 className={styles.title}>
          Lost it on campus?
          <br />
          <span className={styles.accent}>Someone found it.</span>
        </h1>
        <p className={styles.lede}>
          Search posts from fellow Spartans, see exactly where items turned up on the campus map, and
          claim what&rsquo;s yours privately.
        </p>

        <form role="search" method="get" action={CATALOG_URL} className={styles.search}>
          <label htmlFor="hero-q" className={styles.srOnly}>
            Search items
          </label>
          <input
            id="hero-q"
            name="q"
            type="search"
            maxLength={100}
            placeholder="Try “AirPods”, “student ID”, “blue backpack”"
            className={styles.input}
          />
          <button type="submit" className={`btn ${styles.searchBtn}`}>
            Search
          </button>
        </form>

        <div className={styles.popular}>
          <span>Popular:</span>
          {popular.map((name) => (
            <a key={name} href={CATALOG_URL} className={styles.chip}>
              {name}
            </a>
          ))}
        </div>
      </div>
    </section>
  )
}

export default Hero
