import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchMetrics = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/dashboard');
      const data = await response.json();
      setMetrics(data);
      setLoading(false);
    } catch (error) {
      console.error("Error fetching metrics:", error);
    }
  };

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 30000);
    return () => clearInterval(interval);
  }, []);

  if (loading) return <div className="loading">Loading Analytics...</div>;

  return (
    <div className="dashboard">
      <header>
        <h1>Real-Time Analytics</h1>
        <p className="refresh-note">Auto-refreshes every 30 seconds</p>
      </header>

      <div className="grid">
        
        <div className="card">
          <h2>Active Users</h2>
          <div className="big-number">{metrics.activeUsers}</div>
          <p className="subtitle">Last 5 Minutes</p>
        </div>

        
        <div className="card">
          <h2>Top 5 Pages</h2>
          <p className="subtitle">Last 15 Minutes</p>
          <table>
            <thead>
              <tr>
                <th>Page URL</th>
                <th>Views</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(metrics.topPages).map(([url, count]) => (
                <tr key={url}>
                  <td>{url}</td>
                  <td>{count}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        
        <div className="card">
          <h2>Active Sessions</h2>
          <p className="subtitle">Per User (Last 5 mins)</p>
          <div className="session-list">
            {Object.entries(metrics.activeSessions).slice(0, 5).map(([user, count]) => (
              <div key={user} className="session-row">
                <span>{user}</span>
                <span className="badge">{count}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;