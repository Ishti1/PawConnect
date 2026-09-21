import React from 'react';
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import { Home, Heart, MessageCircle, MapPin } from 'lucide-react';
import FeedPage from './pages/FeedPage';
import './index.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app-container">
        {/* Header */}
        <header className="app-header glass">
          <div className="brand">PawConnect</div>
        </header>

        {/* Main Content Area */}
        <main className="content-area">
          <Routes>
            <Route path="/" element={<FeedPage />} />
            <Route path="/adoption" element={<div className="p-4 text-center mt-10">Adoption Coming Soon</div>} />
            <Route path="/donations" element={<div className="p-4 text-center mt-10">Donations Coming Soon</div>} />
            <Route path="/chat" element={<div className="p-4 text-center mt-10">Chat Coming Soon</div>} />
          </Routes>
        </main>

        {/* Bottom Navigation */}
        <nav className="bottom-nav glass">
          <NavLink to="/" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <Home size={24} />
            <span>Feed</span>
          </NavLink>
          <NavLink to="/adoption" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <Heart size={24} />
            <span>Adopt</span>
          </NavLink>
          <NavLink to="/donations" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <MapPin size={24} />
            <span>Donate</span>
          </NavLink>
          <NavLink to="/chat" className={({isActive}) => `nav-item ${isActive ? 'active' : ''}`}>
            <MessageCircle size={24} />
            <span>Chat</span>
          </NavLink>
        </nav>
      </div>
    </BrowserRouter>
  );
}

export default App;
