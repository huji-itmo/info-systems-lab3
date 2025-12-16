FROM oven/bun:1 AS base
WORKDIR /usr/src/app

# зависимости
COPY package.json bun.lock* ./
RUN bun install --frozen-lockfile

# код
COPY . .

# build-time переменная — попадёт в браузерный бандл
# ARG NEXT_PUBLIC_API_URL
# ARG NEXT_PUBLIC_APP_URL
# ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
# ENV NEXT_PUBLIC_APP_URL=${NEXT_PUBLIC_APP_URL}

# prod-режим (и для dev сборки тоже ок)
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# сборка и старт
RUN bun run build

USER bun
EXPOSE 3008
CMD ["bun", "run", "start"]
