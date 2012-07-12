# Change default ports of the HTTP connector for service and administration requests
http:
  port: 10090
  adminPort: 10091

databus:
  maxNumEventsPerDatabusPoll: 2000

# Configure the HTTP client that Polloi uses to make outbound requests (primarily to Databus).
httpClient:
  connectionTimeout: 5s       # Timeout after 1 second while connecting.
  timeout: 10s                # Timeout after 10 seconds while reading or writing.
  timeToLive: 10m             # Keep connections open for 10 minutes.
  cookiesEnabled: false       # Don't track cookies.
  gzipEnabled: true           # Allow for gzipped request and response entities.
  minThreads: 1               # Thread pool for JerseyClient's async requests.
  maxThreads: 10              # Thread pool for JerseyClient's async requests.


# Configure the ZooKeeper connection used for SOA service discovery
zooKeeper:
  connectString: ec2-23-22-29-111.compute-1.amazonaws.com:2181     # ZooKeeper connection string that looks like "host:port,host:port,...".  It should include all members of the ZooKeeper ensemble.
  retryNTimes:                      # Retry policy for connecting to the ZooKeeper server itself
      n: 10                          # This should be higher on production.  10 maybe?  Increasing it pollutes the log if ZK is really not available.
      sleepMsBetweenRetries: 100

# Configure the ElasticSearch cluster to use for search
elasticSearch:
  clusterName: elasticsearch
  hostName: ec2-23-22-29-111.compute-1.amazonaws.com
  port: 9300

# Override ZooKeeper and connect to specified EmoDB System of Record and Databus instances.
# This is useful if EmoDB is running in a different data center and the IP
# addresses it registers in ZooKeeper aren't reachable (ie. EC2 internal IPs).
sorEndPointOverrides:
  ec2-23-22-29-111.compute-1.amazonaws.com: {}

dbusEndPointOverrides:
  ec2-23-22-29-111.compute-1.amazonaws.com: {}


logging:
  loggers:
    "com.bazaarvoice.emodb.examples": INFO
    "org.apache.zookeeper": ERROR               #ZooKeeper is pretty chatty at the WARN level
    "com.netflix.curator.ConnectionState": OFF  # keep local development environment free from known exceptions
