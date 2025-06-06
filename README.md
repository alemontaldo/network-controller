# Overview

with this db (cd /Users/alessandro.montaldo/Projects/ales/java/graphql/localstack && docker compose up -d)

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
    addDevice(input: {mac: "AA:BB:CC:DD:EE:FF", deviceType: ACCESS_POINT}) {
        mac
        uplinkMac
    }
}
```

```graphql
mutation {
    deleteDevice(mac: "AA:BB:CC:DD:EE:FF") {
        success
        message
        deletedMac
    }
}
```

## Build

on mac:
colima stop
colima start --memory 8 --cpu 4
(cd /Users/alessandro.montaldo/Projects/ales/java/graphql/athletic-app && docker build -t athletic-app-builder -f docker/Dockerfile.compileAndRuntime .)

docker create --name temp-container athletic-app-builder
docker cp temp-container:/home/app/athletic-app /Users/alessandro.montaldo/Projects/ales/java/graphql/athletic-app/build/native/nativeCompile/athletic-app
docker rm temp-container

(cd /Users/alessandro.montaldo/Projects/ales/java/graphql/athletic-app && scp -r * ale@aldebaran:/srv/aldebaran/athletic-app/repo/)

and there:

(cd /srv/aldebaran/athletic-app/repo && docker build -t athletic-app -f docker/Dockerfile .)




```bash
(cd /Users/alessandro.montaldo/Projects/ales/java/graphql/athletic-app && docker build -t athletic-app -f docker/Dockerfile .)
(cd /Users/alessandro.montaldo/Projects/ales/java/graphql/athletic-app && docker build -t athletic-app-builder -f docker/Dockerfile.compileAndRuntime .)
```

```bash
docker tag athletic-app aldebaran:5000/athletic-app
```

```bash
docker push aldebaran:5000/athletic-app
```

Spring for GraphQL 1.3 app to demo the following:

- Controller methods on virtual threads via `spring.threads.virtual.enabled` property.
- Synchronous client access in [ClientApp](src/main/java/com/alesmontaldo/activity/ClientApp.java).
- Use of DGS generated client API in [ClientDgsApp](src/main/java/com/alesmontaldo/activity/ClientDgsApp.java).
- Mapping to interface field `Activity.comments`.
- Schema inspection of unions and interfaces.

# Running

Run [ServerApp](src/main/java/com/alesmontaldo/activity/ServerApp.java) from your IDE, or
`./gradlew bootRun` from the command line to start the server.

Then use any of the following:

- Run [ClientApp](src/main/java/com/alesmontaldo/activity/ClientApp.java), or `./gradlew clientRun` from the command line.
- Run [ClientDgsApp](src/main/java/com/alesmontaldo/activity/ClientDgsApp.java), or `./gradlew clientDgsRun`.
- Run slice tests [AthleteControllerTests](src/test/java/com/alesmontaldo/activity/AthleteControllerTests.java) or [SearchControllerTests](src/test/java/com/alesmontaldo/activity/SearchControllerTests.java).
- Open GraphiQL in a browser at http://localhost:8080/graphiql.
