import { fetchResources, type ApiResources } from '../api';
import { mockResources } from '../mock';
import { useRemote } from '../useRemote';

export function ResourceDashboard() {
  const { leases } = useRemote<ApiResources>(fetchResources, mockResources);
  return (
    <section className="panel resources">
      <h2>Resources</h2>
      <table>
        <thead>
          <tr>
            <th>Resource</th>
            <th>Owner</th>
            <th>Expires</th>
          </tr>
        </thead>
        <tbody>
          {leases.map((lease) => (
            <tr key={lease.resource}>
              <td>{lease.resource}</td>
              <td>{lease.owner}</td>
              <td>{lease.expires}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
