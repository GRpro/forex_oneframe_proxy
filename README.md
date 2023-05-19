# OneFrame proxy implementation
This setup consists of 2 services. External service `OneFrame` which has a limitation for number of requests per token. Internal service `Forex` which acts as a proxy for OneFrame API and bypasses SLA with caching.
This repository is an implementation of `Forex` service. It's API provides an HTTP endpoint providing currency exchange rates for a given currency pair.

## Requirements and limitations

- [Requirements](https://github.com/paidy/interview/blob/master/Forex.md)
- [OneFrame image](https://hub.docker.com/r/paidyinc/one-frame)

There are 3 functional requirements:
1. The service returns an exchange rate when provided with 2 supported currencies
   - Assumed the following list of currencies is supported `AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD`.

2. The service should support at least 10,000 successful requests per day with 1 API token
   - The service supports any number of requests per day for a single configured `OneFrame` API token by utilizing a cache.

3. The rate should not be older than 5 minutes
   - `OneFrame` supports only 1000 requests per day meaning no more than 1000 / 24 =~ 42 requests per hour, which means a delay of at least 3600 / 42 ~= 86 seconds between requests on average.
      To support this requirement I need to update the cache every 300 seconds which is acceptable by the API SLA.
      The setting `app.cache.update-interval` in `application.conf` should be in range `[86, 300] seconds`.

## Service setup and execute request

1. Pull OneFrame image and start service
    ```bash
    docker pull paidyinc/one-frame:latest
    docker run -p 8080:8080 paidyinc/one-frame 
    ```
   
2. Export token and run with sbt. Note, the `run` command triggers scala project build and might take few minutes to download dependencies and compile
    ```bash
    export ONE_FRAME_TOKEN=10dc303535874aeccc86a8251e6992f5
    sbt run
    ```
   
3. Example request
   ```bash
   curl 'localhost:8082/rates?from=USD&to=JPY'
   ```

## Troubleshooting
Check if OneFrame is accepting calls
```bash
curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/rates?pair=USDJPY'
```
If not, check the docker container is running, try restarting it in case number of requests to OneFrame service might have exceeded the 1000 limit

