import React, {useEffect, useState} from 'react';
import axios from 'axios';
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

function App() {
  const [token, setToken] = useState('');
  const [symbol, setSymbol] = useState('');
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setToken(savedToken);
    }
  }, []);

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    }
  }, [token]);

  const handleLogin = async () => {
    try {
      const response = await axios.post('http://localhost:8080/login', {
        username: 'trader',
        password: 'password123'
      });
      console.log("Login response:", response.data);
      setToken(response.data);
    } catch (err) {
      setError('Login failed');
    }
  };

  const fetchStockData = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/stock/analysis?symbol=${symbol}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setData(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch data');
    }
  };

  const chartData = data ? {
    labels: ['Short-Term SMA', 'Long-Term SMA'],
    datasets: [{
      label: 'SMA',
      data: [data.shortTermSMA, data.longTermSMA],
      borderColor: 'blue',
      fill: false
    }]
  } : null;

  return (
      <div style={{ padding: '20px' }}>
        <h1>Stock Monitor</h1>
        {!token ? (
            <div>
              <button onClick={handleLogin}>Login</button>
              {error && <p>{error}</p>}
            </div>
        ) : (
            <div>
              <input
                  type="text"
                  value={symbol}
                  onChange={(e) => setSymbol(e.target.value)}
                  placeholder="Enter stock symbol (e.g., AAPL)"
              />
              <button onClick={fetchStockData}>Analyze</button>
              {error && <p>{error}</p>}
              {data && (
                  <div>
                    <h2>{data.symbol}</h2>
                    <p>Short-Term SMA: {data.shortTermSMA}</p>
                    <p>Long-Term SMA: {data.longTermSMA}</p>
                    <p>Trading Signal: {data.tradingSignal}</p>
                    <Line data={chartData} />
                  </div>
              )}
            </div>

        )}
      </div>
  );
}

export default App;
