import { MAP_URL } from '../config.js'
import styles from './MapSection.module.css'

// A decorative preview of the campus map. The real map lives in the backend at /map.
function MapSection() {
  return (
    <section className={`container ${styles.section}`}>
      <div className={styles.panel}>
        <div className={styles.text}>
          <h2 className="section-title">See it on the map</h2>
          <p className={styles.body}>
            Every post is pinned where the item was lost or found — retrace your steps from the library to
            the Event Center.
          </p>
          <a href={MAP_URL} className="btn btn-dark">
            Open campus map
          </a>
        </div>
        <div className={styles.preview} role="img" aria-label="Campus map preview with item pins">
          <span className={styles.block} style={{ left: '18%', top: '22%', width: '24%', height: '20%' }} />
          <span className={styles.block} style={{ left: '52%', top: '18%', width: '30%', height: '26%' }} />
          <span className={styles.block} style={{ left: '28%', top: '56%', width: '34%', height: '22%' }} />
          <span className={`${styles.dot} ${styles.blue}`} style={{ left: '28%', top: '30%' }} />
          <span className={`${styles.dot} ${styles.gold}`} style={{ left: '64%', top: '28%' }} />
          <span className={`${styles.dot} ${styles.blue}`} style={{ left: '44%', top: '64%' }} />
          <span className={`${styles.dot} ${styles.gold}`} style={{ left: '72%', top: '62%' }} />
          <div className={styles.popup}>
            <strong>AirPods Pro case</strong>
            <span>Found · Student Union</span>
          </div>
        </div>
      </div>
    </section>
  )
}

export default MapSection
