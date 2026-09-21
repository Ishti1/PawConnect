import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'https://pawconnect-1-bhp8.onrender.com/api';

export default function FeedPage() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch all posts from the backend
    axios.get(`${API_URL}/posts`)
      .then(res => {
        // Sort newest first
        const sorted = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setPosts(sorted);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error fetching posts:', err);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div className="loader">Loading pawsome posts... 🐾</div>;
  }

  return (
    <div>
      {posts.map(post => (
        <div key={post.id} className="post-card">
          <div className="post-header">
            <div className="avatar">
              {post.authorName ? post.authorName.charAt(0).toUpperCase() : 'U'}
            </div>
            <div>
              <div className="post-author">{post.authorName || 'Anonymous User'}</div>
              <div className="post-time">
                {new Date(post.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
              </div>
            </div>
          </div>
          <div className="post-content">
            {post.content}
          </div>
          {post.imageUrl && (
            <img src={post.imageUrl} alt="Cat" className="post-image" loading="lazy" />
          )}
        </div>
      ))}

      {posts.length === 0 && (
        <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
          Coming Soon!
        </div>
      )}
    </div>
  );
}
