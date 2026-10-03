import { fetchAgents, type ApiAgents } from '../api';
import { mockAgents } from '../mock';
import { useRemote } from '../useRemote';

export function AgentCards() {
  const { agents } = useRemote<ApiAgents>(fetchAgents, mockAgents);
  return (
    <section className="panel agents">
      <h2>Agents</h2>
      <ul>
        {agents.map((agent) => (
          <li key={agent.id} className="agent-card">
            <div className="agent-head">
              <strong>{agent.id}</strong>
              <span className={`status status-${agent.status}`}>{agent.status}</span>
            </div>
            <dl>
              <dt>template</dt>
              <dd>{agent.template}</dd>
              <dt>model</dt>
              <dd>{agent.model}</dd>
            </dl>
          </li>
        ))}
      </ul>
    </section>
  );
}
