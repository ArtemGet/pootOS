import { fetchHealth, type Health } from '../api';
import { useRemote } from '../useRemote';

const offline: Health = { status: 'offline' };

export function TopBar() {
  const health = useRemote<Health>(fetchHealth, offline);
  return (
    <header className="topbar">
      <span className="brand">pootOS</span>
      <div className="chat-placeholder" role="textbox" aria-label="System Agent chat">
        Ask the System Agent…
      </div>
      <span className={`health health-${health.status}`}>{health.status}</span>
    </header>
  );
}
