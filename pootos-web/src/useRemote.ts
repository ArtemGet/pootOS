import { useEffect, useState } from 'react';

export function useRemote<T>(load: () => Promise<T>, fallback: T): T {
  const [value, setValue] = useState<T>(fallback);
  useEffect(() => {
    let active = true;
    load().then(
      (data) => {
        if (active) {
          setValue(data);
        }
      },
      () => {
        if (active) {
          setValue(fallback);
        }
      },
    );
    return () => {
      active = false;
    };
  }, []);
  return value;
}
