import FadeIn from './FadeIn.jsx'
import styles from './Features.module.css'

const features = [
  {
    title: 'Report an Item',
    description:
      'Create a post for an item you lost or found on campus so other students can see it.',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
        <path d="M12 5v14M5 12h14" />
      </svg>
    ),
  },
  {
    title: 'Search Item Posts',
    description:
      'Browse and filter lost-and-found posts to quickly spot an item that matches yours.',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
      </svg>
    ),
  },
  {
    title: 'Connect With Students',
    description:
      'Message other students to verify ownership and coordinate the return of an item.',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M21 11.5a8.5 8.5 0 0 1-8.5 8.5c-1.5 0-2.9-.4-4.1-1L3 20l1-5.4A8.5 8.5 0 1 1 21 11.5Z" />
      </svg>
    ),
  },
]

function Features() {
  return (
    <section className={styles.features}>
      <div className="container">
        <FadeIn>
          <span className="eyebrow">Features</span>
          <h2 className="section-title">Everything you need to recover an item</h2>
        </FadeIn>
        <div className={styles.grid}>
          {features.map(({ title, description, icon }, index) => (
            <FadeIn key={title} delay={index * 120} className={styles.cardWrap}>
              <article className={styles.card}>
                <span className={styles.icon}>{icon}</span>
                <h3>{title}</h3>
                <p>{description}</p>
              </article>
            </FadeIn>
          ))}
        </div>
      </div>
    </section>
  )
}

export default Features
