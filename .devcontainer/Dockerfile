FROM mcr.microsoft.com/devcontainers/java:17

# Install Node + Angular CLI
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g @angular/cli

WORKDIR /workspace
