# OneFrame proxy implementation
This setup consists of 2 services. External service `OneFrame` which has a limitation for number of requests per token. Internal service `Forex` which acts as a proxy for OneFrame API and bypasses SLA with caching.
This repository is an implementation of `Forex` service. It's API provides an HTTP endpoint providing currency exchange rates for a given currency pair.

## Links

1. [Requirements](https://github.com/paidy/interview/blob/master/Forex.md)
2. [OneFrame image](https://hub.docker.com/r/paidyinc/one-frame)

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
   curl -v 'localhost:8082/rates?from=USD&to=JPY'
   ```

## Troubleshooting
Check if OneFrame is accepting calls
```bash
curl -H "token: 10dc303535874aeccc86a8251e6992f5" 'localhost:8080/rates?pair=USDJPY'
```
If not, check the docker container is running, try restarting it in case number of requests to OneFrame service might have exceeded the 1000 limit

