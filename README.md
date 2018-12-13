Hello WebSocket Play Server
=================================

A simple websocket server which answers "Hello!" and could close the connection

# Run

```
sbt run
```

The server is listening on the route :
```
GET /
```

You can specify query parameter `close` to `true` to force the server to close the WebSocket connection after sending "Hello!"