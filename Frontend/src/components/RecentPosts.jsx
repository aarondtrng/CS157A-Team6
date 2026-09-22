import { useEffect, useState } from 'react'
import FadeIn from './FadeIn.jsx'
import styles from './RecentPosts.module.css'

const recentPosts = [
  {
    title: 'Black Water Bottle',
    type: 'Found Item',
    location: 'Student Union',
    date: 'Today',
  },
  {
    title: 'Blue Backpack',
    type: 'Lost Item',
    location: 'Engineering Building',
    date: 'Yesterday',
  },
  {
    title: 'Apple AirPods',
    type: 'Found Item',
    location: 'King Library',
    date: '2 days ago',
  },
]

function RecentPosts() {
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Temporary delay for the demo.
    // Later, this can be replaced with a backend request.
    const timer = setTimeout(() => {
      setLoading(false)
    }, 900)

    return () => clearTimeout(timer)
  }, [])

  return (
    <section id="recent-posts" className={styles.recentPosts}>
      <div className="container">
        <FadeIn>
          <div className={styles.header}>
            <span className={styles.eyebrow}>Recent Activity</span>

            <h2 className={styles.title}>
              Recently Reported Items
            </h2>

            <p className={styles.subtitle}>
              Browse the latest lost-and-found posts from around campus.
            </p>
          </div>
        </FadeIn>

        {loading ? (
          <div className={styles.loading} role="status">
            <div className={styles.spinner}></div>
            <p>Loading recent posts...</p>
          </div>
        ) : (
          <div className={styles.grid}>
            {recentPosts.map((post) => (
              <article className={styles.card} key={post.title}>
                <span className={styles.icon}>+</span>
                <span className={styles.type}>{post.type}</span>
                <h3>{post.title}</h3>
                <p>{post.location}</p>
                <small>{post.date}</small>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

export default RecentPosts
