# Network controller

## introduction


## assignment description


## Details about this implementation

This implementation enforces a strict acyclic topology.
Devices cannot uplink to their descendants or create cycles in the network structure.
More precisely the network can be though as a forest of trees
(devices can be unconnected and multiple unconnected regions of networks are allowed).

two implementations are available for persistence:
- in memory storage (use spring profile active: in-memory)
- mongo DB (recommended. there is a docker compose for that)

## Run the application

if you want to run the app with MongoDB storage, deploy localstack:

```bash
(cd localstack && docker compose up -d)
```

this will create two containers:
1. MongoDB
2. Mongo Express (a MongoDB GUI that will be available at: http://localhost:8081/

for development, run the app with:
```bash

```

After starting the app a GraphQL client is available at: http://localhost:8080/graphiql?path=/graphql

## Queries and mutations available

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

