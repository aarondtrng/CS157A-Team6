import NavBar from './components/NavBar.jsx'
import Hero from './components/Hero.jsx'
import RecentPosts from './components/RecentPosts.jsx'
import HowItWorks from './components/HowItWorks.jsx'
import MapSection from './components/MapSection.jsx'
import CallToAction from './components/CallToAction.jsx'
import Footer from './components/Footer.jsx'

function App() {
  return (
    <>
      <NavBar />
      <main>
        <Hero />
        <RecentPosts />
        <HowItWorks />
        <MapSection />
        <CallToAction />
      </main>
      <Footer />
    </>
  )
}

export default App
