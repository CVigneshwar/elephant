FROM mcr.microsoft.com/devcontainers/java:17

# Install Node using NVM (recommended for Dev Containers)
ENV NVM_DIR=/usr/local/nvm
ENV NODE_VERSION=18

RUN mkdir -p $NVM_DIR \
    && curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash \
    && . $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \
    && nvm use $NODE_VERSION \
    && nvm alias default $NODE_VERSION \
    && ln -s $NVM_DIR/versions/node/v$NODE_VERSION/bin/node /usr/bin/node \
    && ln -s $NVM_DIR/versions/node/v$NODE_VERSION/bin/npm /usr/bin/npm \
    && npm install -g @angular/cli

WORKDIR /workspace
