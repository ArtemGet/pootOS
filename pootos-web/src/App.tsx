import { TopBar } from './components/TopBar';
import { AttentionZones } from './components/AttentionZones';
import { ContextGraph } from './components/ContextGraph';
import { AgentCards } from './components/AgentCards';
import { ResourceDashboard } from './components/ResourceDashboard';

export function App() {
  return (
    <div className="app">
      <TopBar />
      <main className="content">
        <div className="main-col">
          <AttentionZones />
          <ContextGraph />
        </div>
        <aside className="side-col">
          <AgentCards />
          <ResourceDashboard />
        </aside>
      </main>
    </div>
  );
}
