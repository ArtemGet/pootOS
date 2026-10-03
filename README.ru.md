# pootOS

Агентная операционная система — песочница для автономных агентов: ядро с durable-акторами и лизами
ресурсов, content-addressed **контекст-граф** вместо чата на задачу, зоны внимания и системный
агент, конфигурирующий всё в рантайме. Бэкенд: **Java 25 (Elegant Objects)**; web UI: **React + TypeScript**.

> Языки: **en** ([README.md](README.md)) · **ru** (этот файл) · [zh-CN](README.zh-CN.md)

## Статус

Ранняя разработка (до 0.1.0). **Запускать пока нечего** — заложен фундамент (примитивы графа,
SPI песочницы, SPI исполнителя, лизы ресурсов ядра, security-гейты в CI). См.
[`docs/ROADMAP.md`](docs/ROADMAP.md).

## Быстрый старт (сборка из исходников)

Требуется **JDK 25** и **Maven** (Docker понадобится позже, для запуска агентов).

```bash
git clone https://github.com/ArtemGet/pootOS.git
cd pootOS
# Windows PowerShell:
$env:JAVA_HOME = "C:\Users\<you>\.jdks\temurin-25"
mvn --errors --batch-mode clean install -Pqulice -Pjtcop
```

Запускаемого приложения `pootos` пока нет (цель — 0.1.0, web-first).

## Документация

- Архитектура: **ru** (этот) · [en](docs/ARCHITECTURE.en.md) · [zh-CN](docs/ARCHITECTURE.zh-CN.md)
- [Безопасность](docs/SECURITY.md) · [Параллельные воркспейсы](docs/PARALLELISM.md)
- [ADR](docs/adr/) · [Roadmap](docs/ROADMAP.md) · [Контракт контрибьютора](AGENTS.md)

## Модули

`pootos-context` · `pootos-agent` · `pootos-sandbox` · `pootos-kernel` (дальше — больше).

## Лицензия

MIT — см. [LICENSE](LICENSE).
