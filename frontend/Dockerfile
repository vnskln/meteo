FROM node:26.1.0-alpine AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM caddy:2.11.7-alpine AS runtime
COPY Caddyfile /etc/caddy/Caddyfile
COPY --from=build /build/dist /srv
