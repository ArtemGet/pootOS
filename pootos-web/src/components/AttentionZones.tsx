import { attentionZones, type Zone } from '../mock';

const zones: Zone[] = ['RED', 'YELLOW', 'GREEN'];

export function AttentionZones() {
  return (
    <section className="panel zones">
      <h2>Attention zones</h2>
      <div className="zone-columns">
        {zones.map((zone) => (
          <div key={zone} className={`zone zone-${zone.toLowerCase()}`}>
            <h3>{zone}</h3>
            <ul>
              {attentionZones[zone].map((card) => (
                <li key={card.id} className="card">
                  <span className="card-kind">{card.kind}</span>
                  {card.title}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </section>
  );
}
