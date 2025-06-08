## TODO:
optional validation hook configurable

The app supports configurable uplink validation rules to restrict which device types may connect to others, 
but this logic is optional and disabled by default to maximize flexibility.

maybe with something like this:
```java
Map<DeviceType, Set<DeviceType>> allowedUplinkMap = Map.of(
    GATEWAY, Set.of(), // can't uplink to anything
    SWITCH, Set.of(GATEWAY),
    ACCESS_POINT, Set.of(GATEWAY, SWITCH)
);
```

# Notes

This implementation enforces a strict acyclic topology. Devices cannot uplink to their descendants or create cycles in the network structure.

Future work:
- Delete Functionality (Not Implemented):
In a real-world scenario, network devices are added and removed. 
A safe deletion feature (e.g., with cascading rules) could be added to maintain topology integrity. 
However, since it is not part of the original requirements, it is not included in this implementation.


# Overview

for development:

deploy localstack:
(cd /Users/alessandro.montaldo/Projects/ales/demo/network-controller/localstack && docker compose up -d)

db browser at: http://localhost:8081/

at: http://localhost:8080/graphiql?path=/graphql

```graphql
{
  deviceByMac(mac: "AA:BB:CC:DD:EE:FF") {
    mac
    uplinkMac
    downlinkDevices {
      mac
    }
  }
}
```

```graphql
mutation {
    addDevice(
        input: {
            mac: "CC:CC:CC:CC:CC:CC",
            deviceType: SWITCH,
            #uplinkMac: "BB:BB:BB:BB:BB:BB"
        }
    ) {
        ... on Device {
            mac
            uplinkMac
            deviceType
        }
        ... on ValidationError {
            message
        }
        ... on ServerError {
            message
            errorCode
        }
    }
}
```

```graphql
{
  subtree(mac: "BB:BB:CC:DD:EE:FF") {
    mac
    uplinkMac
    deviceType
    downlinkDevices {
      mac
      uplinkMac
      deviceType
      downlinkDevices {
        mac
        uplinkMac
        deviceType
      }
    }
  }
}
```
