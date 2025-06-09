# Network Controller

A GraphQL API for managing network device deployment with support for various device types and hierarchical relationships.
A network deployments may consist of various networking devices (Gateways, Switches, and Access Points).
Through the API the user can register devices, query devices and network topology all while the network controller
ensures the integrity of the network structure.

## Features

- Register networking devices (Gateways, Switches, Access Points) to a network deployment
- Retrieve all registered devices sorted by device type
- Look up specific devices by MAC address
- View the complete network topology as a forest of trees structure
- View subtrees of the network starting from any device

## Network Topology Rules

This implementation enforces a strict acyclic topology. Devices cannot uplink to their descendants or create cycles in the network structure. 
More precisely, the network can be thought of as a forest of trees because devices can be unconnected and multiple unconnected 
regions of networks are allowed.

## Technology Stack

- Java 21
- Spring Boot
- GraphQL
- MongoDB for persistence

## Persistence Options

Two implementations are available for persistence:
- **In-memory storage**: Use Spring profile `in-memory`
- **MongoDB**: (Docker Compose configuration provided)

## Running the Application

### Prerequisites

- Java 21 or higher
- (Optional) Docker and Docker Compose for MongoDB

### Setup MongoDB (optional but recommended)

Deploy the MongoDB container using Docker Compose:

```bash
(cd localstack && docker compose up -d)
```

This will create two containers:
1. **MongoDB**: The database server
2. **Mongo Express**: A MongoDB GUI available at http://localhost:8081/

### Running the Application

If you started the localstack, run the app with MongoDB this way:

```bash
./gradlew bootRun
```

If you prefer instead the in-memory storage:

```bash
./gradlew bootRun --args='--spring.profiles.active=in-memory'
```

After starting the app, a GraphQL client is available at: http://localhost:8080/graphiql?path=/graphql

Note: the app can be compiled to native executable 
(faster startup, smaller memory footprint). You can generate the executable with:

```bash
./gradlew clean nativeCompile
```

this will generate an executable at: build/native/nativeCompile/network-controller
that you can simply execute from your terminal.


## API Documentation

### GraphQL Schema

The API provides the following operations:

1. **Registering a device to a network deployment:**
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

2. **Retrieving all registered devices, sorted by `deviceType`**
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

3. **Retrieving network deployment device by MAC address:**
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

4. **Retrieving all registered network device topology**
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

5. **Retrieving network device topology starting from a specific device.**
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

## Error Handling

The API uses union types to provide detailed error information:

- **ValidationError**: Returned when input validation fails (e.g., invalid MAC address, non-existent uplink device)
- **ServerError**: Returned for internal server errors with an error code

## Implementation Details

- **Concurrency Control**: Uses distributed locking to prevent race conditions when modifying the network topology.
  The primary concern there is to prevent cycles in the network topology. For the Mongo DB persistence the lock is 
  achieved with a lock with set TTL stored in DB that needs to be acquired before updating the topology. 
  In the in-memory implementation the lock is simply a JVM synchronization block. 
- **Retry Mechanism**: Implements Spring Retry for handling concurrent modification exceptions
- **JSON Representation**: Uses GraphQL JSON scalar for representing complex tree structures
- The repository aims to be structured following the Domain Driven Design principles.
- The code is provided with needed hints to support native compilation with GraalVM

## Building and Testing

Build the project:

```bash
./gradlew clean build
```

Run tests: (requires Mongo DB running)

```bash
./gradlew test
```

## Database Management

To reset the database and start fresh I recommend shutting down the local stack, then:

```bash
docker volume rm localstack_mongodb_data
```

and starting back on the localstack

## License

[MIT License](LICENSE)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Debug

for GraphQL troubleshooting I recommend these properties:
```properties
logging:
  level:
    com.alesmontaldo.network_controller: DEBUG
    org.springframework.graphql: DEBUG
    org.springframework.graphql.data.method.annotation.support.AnnotatedControllerConfigurer: TRACE
```
