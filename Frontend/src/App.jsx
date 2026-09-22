import NavBar from './components/NavBar.jsx'
import Hero from './components/Hero.jsx'
import Stats from './components/Stats.jsx'
import Features from './components/Features.jsx'
import CallToAction from './components/CallToAction.jsx'
import Footer from './components/Footer.jsx'
import RecentPosts from './components/RecentPosts.jsx'

function App() {
  return (
    <>
      <NavBar />
      <main>
        <Hero />
        <Stats />
        <Features />
        <RecentPosts />
        <CallToAction />
      </main>
      <Footer />
    </>
  )
}

export default App
