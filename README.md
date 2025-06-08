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

## Tasks

1. Registering a device to a network deployment:
   input: `deviceType`, `macAddress`, `uplinkMacAddress`
    
    ```graphql
    mutation {
        addDevice(
            input: {deviceType: ACCESS_POINT, macAddress: "BB:BB:BB:BB:BB:BB", uplinkMacAddress: "AA:AA:AA:AA:AA:AA"}
        ) {
            ... on Gateway {
                macAddress
                uplinkMacAddress
                deviceType
            }
            ... on Switch {
                macAddress
                uplinkMacAddress
                deviceType
            }
            ... on AccessPoint {
                macAddress
                uplinkMacAddress
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

2. Retrieving all registered devices, sorted by `deviceType`
   output: sorted list of devices, where each entry has `deviceType` and `macAddress` 
   (sorting order: `Gateway` > `Switch` > `Access Point`)

   ```graphql
   {
      allDevicesSorted {
         ... on DeviceResultView {
            macAddress
            deviceType
         }
         ... on ServerError {
            message
            errorCode
         }
      }
   }
   ```

3. Retrieving network deployment device by MAC address:
   input: `macAddress`
   output: Device entry, which consists of `deviceType` and `macAddress`
    
   ```graphql
   {
      getDevice(macAddress: "AA:AA:AA:AA:AA:AA") {
         ... on DeviceResultView {
            macAddress
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

4. Retrieving all registered network device topology
   output: `Device topology` as tree structure, node should be represented as `macAddress`

    ```graphql
   {
     fullTopology {
       ... on JsonResult {
         data
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

5. Retrieving network device topology starting from a specific device.
   input: `macAddress`
   output: `Device topology` where root node is device with matching macAddress 

    ```graphql
   {
     deviceTopology(macAddress: "AA:AA:AA:AA:AA:AA") {
       ... on JsonResult {
         data
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

