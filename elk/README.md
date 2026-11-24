
# ELK Stack – WebDisplay Centralized Logging

This folder contains the ELK (Elasticsearch, Logstash, and Kibana) stack for collecting, storing, and visualizing WebDisplay application logs.

## Components

- **Elasticsearch:** Stores and indexes application logs.
- **Logstash:** Reads application log files and sends them to Elasticsearch.
- **Kibana:** Provides a dashboard to search, filter, and analyze logs.

## Prerequisites

- Docker Desktop installed and running.
- WebDisplay application configured to generate logs.

## Project Structure

```text
elk/
├── docker-compose.yml
├── logstash.conf
└── README.md
```

## Start the ELK Stack

From the WebDisplay project root directory, run:

```bash
docker compose -f elk/docker-compose.yml up -d
```

Check the running containers:

```bash
docker compose -f elk/docker-compose.yml ps
```

## Access the Services

| Service | URL |
|---|---|
| Elasticsearch | http://localhost:9200 |
| Kibana | http://localhost:5601 |

## View Application Logs

1. Start the WebDisplay Spring Boot application.
2. Ensure the application generates logs in the `logs/` directory.
3. Open Kibana at http://localhost:5601.
4. Navigate to **Discover**.
5. Create a data view using the index pattern:

```text
application-logs-*
```

6. Select `@timestamp` as the time field.
7. View and filter the collected application logs.

## Stop the ELK Stack

```bash
docker compose -f elk/docker-compose.yml stop
```

To start it again:

```bash
docker compose -f elk/docker-compose.yml up -d
```

## Important Notes

- Application log files are generated locally and are not committed to GitHub.
- Elasticsearch data is stored in a Docker named volume to preserve indexed logs across container restarts.
- Docker Compose starts Elasticsearch, Logstash, and Kibana together.
