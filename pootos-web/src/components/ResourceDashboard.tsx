import { resources } from '../mock';

export function ResourceDashboard() {
  return (
    <section className="panel resources">
      <h2>Resources</h2>
      <table>
        <thead>
          <tr>
            <th>Resource</th>
            <th>TTL</th>
            <th>Budget</th>
            <th>Cost</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {resources.map((resource) => (
            <tr key={resource.id} className={resource.stalled ? 'stalled' : undefined}>
              <td>{resource.name}</td>
              <td>{resource.leaseTtl}</td>
              <td>{resource.budget}</td>
              <td>{resource.cost}</td>
              <td>
                <button type="button" disabled>
                  {resource.stalled ? 'restart' : 'stop'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
