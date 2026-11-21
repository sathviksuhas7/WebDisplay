# ELK Stack – Local WebDisplay Log Aggregation

This optional local-development stack collects multiple `.log` files from the WebDisplay `logs/` directory and makes them searchable in Elasticsearch and Kibana.

## Requirements

- Docker Desktop running.
- WebDisplay configured to write logs into the project `logs/` directory.

## Start

From the project root:

```bash
docker compose -f elk/docker-compose.yml up -d
```

Open:
- Elasticsearch: http://localhost:9200
- Kibana: http://localhost:5601

In Kibana, open **Discover** and create a data view:
- Name: `webdisplay-logs-*`
- Timestamp field: choose `@timestamp` if available.

Logstash watches `/var/log/webdisplay/*.log`, so additional `.log` files placed in the project's `logs/` folder are included.

## Stop

```bash
docker compose -f elk/docker-compose.yml down
```

To also delete the local Elasticsearch index data:

```bash
docker compose -f elk/docker-compose.yml down -v
```

**Security:** This compose file disables Elasticsearch security for local development only. Do not expose these ports or use this configuration in production. Use authentication, TLS, network restrictions, retention policies and secured credentials for any shared or production deployment.

The ELK stack is optional and separate from the Spring Boot application. The application can run without Docker or ELK.
