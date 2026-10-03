import { agentCards } from '../mock';

export function AgentCards() {
  return (
    <section className="panel agents">
      <h2>Agents</h2>
      <ul>
        {agentCards.map((agent) => (
          <li key={agent.id} className="agent-card">
            <div className="agent-head">
              <strong>{agent.name}</strong>
              <span className={`status status-${agent.status}`}>{agent.status}</span>
            </div>
            <dl>
              <dt>template</dt>
              <dd>{agent.template}</dd>
              <dt>model</dt>
              <dd>{agent.modelRef}</dd>
              <dt>heartbeat</dt>
              <dd>{agent.heartbeat}</dd>
            </dl>
          </li>
        ))}
      </ul>
    </section>
  );
}
