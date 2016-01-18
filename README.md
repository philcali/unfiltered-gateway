# Unfiltered API Gateway Bindings

This library contains a set of bindings for AWS API Gateway. The purpose
of the library to facilitate reusability with existing unfiltered API's,
that would well enough with Lambda executions.

## Examples

A single plan that handles with zero templating:

```
class MyFunction extends Plan {
  def intent = {
    case req => ResponseString("Hello world!")
  }
}
```

A single lambda _server_ that handles all of the API:

```
class MyApi extends EmbeddedApp with LambdaExecution {
  def server = Server
    .plan(Planify(Resource1.intent))
    .plan(Planify(Resource2.intent))
}
```

If you want to run the same server locally using jetty:

```
object MyApi extends EmbeddedApp {
  type Embedded = Server

  def server = Server
    .plan(Planify(Resource1.intent))
    .plan(Planify(Resource2.intent))
}
MyApi.run(_.http(8080).run())
```

## TODO

- Make a script to generate an API from DSL
