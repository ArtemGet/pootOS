export function TopBar() {
  return (
    <header className="topbar">
      <span className="brand">pootOS</span>
      <div className="chat-placeholder" role="textbox" aria-label="System Agent chat">
        Ask the System Agent…
      </div>
    </header>
  );
}
