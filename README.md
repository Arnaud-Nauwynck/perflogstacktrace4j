# perflog-stacktrace4j

A glue API of ThreadLocal push/pop/log/progress for java, handling Performance stats, Log and Trace/Span


- Enrich java native stack trace with ThreadLocal applicative stack using push/pop

- Enrich Performance Counters (with time/cputime/nb of call/min/max stats) per push/pop with a fast lightweight IN-MEMORY Tree

- Enrich java native stack trace with ThreadLocal applicative stack spans using simple push/pop


- Enrich logs with current app stack context, and enrich logback formatted log text with inherited stack properties + named-values parameter in log messages.
Appender for sending enriched log event to ElasticSearch


--------------------------
Optionnal Connectors / Adapters to thirdparties libraries / protocols:

- logback Appender to ElasticSearch: 
  send structured event log with enriched app stack span, and named-values

- Zipkin(Spring Sleuth) or OpenTracing:
  synchronize to Zipkin trace/spans
  synchronize from Zipkin Tracer/Instrumenter to thread-local app stack

- InfluxdDb or Prometheus exporter:
  Exporter for in-memory Performance Stats Tree
