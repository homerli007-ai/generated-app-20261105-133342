import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError('');
    setResults([]);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) {
      setError('Enter at least one path');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const data = await res.json();
      setResults(data.results || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe File Delete</h1>
      <p className="hint">One absolute path per line. Only regular files inside the allowed root are deleted.</p>
      <form onSubmit={submit}>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          rows={6}
          placeholder="/path/to/file.txt"
        />
        <button type="submit" disabled={loading}>{loading ? 'Deleting...' : 'Delete'}</button>
      </form>
      {error && <div className="error">{error}</div>}
      {results.length > 0 && (
        <table>
          <thead>
            <tr><th>Path</th><th>Deleted</th><th>Reason</th></tr>
          </thead>
          <tbody>
            {results.map((r, i) => (
              <tr key={i} className={r.deleted ? 'ok' : 'fail'}>
                <td>{r.path}</td>
                <td>{r.deleted ? 'yes' : 'no'}</td>
                <td>{r.reason}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
