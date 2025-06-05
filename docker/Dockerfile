# Use exact (slim) Debian build to get consistent results
# You can look up the digest for this tag on Docker Hub if you need an immutable reference.
FROM debian:12.6-slim

# Install what you need and clean up
# (shadow provides useradd; sudo if you ever need it)
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Do not run application as root
RUN useradd --create-home --shell /bin/bash app

USER app

# Setting HOME will make many tools happy
ENV HOME=/home/app
WORKDIR ${HOME}

# Separate directory for config file mounts if necessary
# VOLUME ${HOME}/config

# Set a variable for the native executable name
ENV EXECUTABLE_NAME="athletic-app"

# Copy in the built artifact with the correct path
COPY --chown=app:app /build/native/nativeCompile/${EXECUTABLE_NAME} ${HOME}/

# Ensure the executable has the correct permissions
RUN chmod +x ${HOME}/${EXECUTABLE_NAME}

# Default Spring-Boot config locations
ENV LOCAL_APP_OPTS="--spring.config.location=./config/application.yaml,optional:./config/credentials.properties"
ENV APP_OPTS=""

ENTRYPOINT ["/home/app/athletic-app", "${LOCAL_APP_OPTS}"]
