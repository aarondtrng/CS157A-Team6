import { CATALOG_URL } from '../config.js'
import styles from './RecentPosts.module.css'

// Sample posts for the landing page. This is placeholder content, not real data: the landing page cannot
// read the backend's items (no JSON endpoints), so the real list lives at the catalog.
const samplePosts = [
  { type: 'FOUND', category: 'Electronics', title: 'AirPods Pro case', place: 'Student Union', when: '2h ago' },
  { type: 'LOST', category: 'ID & cards', title: 'Tower Card — J. Nguyen', place: 'MLK Library', when: '5h ago' },
  { type: 'FOUND', category: 'Water bottles', title: 'Navy Hydro Flask, 32 oz', place: 'Engineering Bldg', when: 'Yesterday' },
  { type: 'LOST', category: 'Bags', title: 'Black JanSport backpack', place: 'Event Center', when: '2 days ago' },
]

function RecentPosts() {
  return (
    <section id="recent-posts" className={`container ${styles.section}`}>
      <div className={styles.header}>
        <div>
          <h2 className="section-title">Recently posted</h2>
          <p className="section-subtitle">The newest lost and found reports from around campus.</p>
        </div>
        <a href={CATALOG_URL} className={styles.viewAll}>
          View all items
          <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14M13 6l6 6-6 6" />
          </svg>
        </a>
      </div>

      <div className={styles.grid}>
        {samplePosts.map((post) => (
          <a key={post.title} href={CATALOG_URL} className={styles.card}>
            <div className={styles.photo}>
              <svg aria-hidden="true" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="5" width="18" height="14" rx="2" />
                <circle cx="9" cy="10" r="1.5" />
                <path d="M21 16l-5-5-8 8" />
              </svg>
              <span className={`${styles.badge} ${post.type === 'FOUND' ? styles.found : styles.lost}`}>{post.type}</span>
            </div>
            <div className={styles.body}>
              <span className={styles.category}>{post.category}</span>
              <span className={styles.postTitle}>{post.title}</span>
              <span className={styles.place}>
                <svg aria-hidden="true" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 21s-7-6.2-7-11.5A7 7 0 0 1 19 9.5C19 14.8 12 21 12 21z" />
                  <circle cx="12" cy="9.5" r="2.5" />
                </svg>
                {post.place} · {post.when}
              </span>
            </div>
          </a>
        ))}
      </div>
    </section>
  )
}

export default RecentPosts
