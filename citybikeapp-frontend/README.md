# City Bike App frontend

## Getting started

Requirements
* Node/npm. Developed on Node v18 so should work at least on that.
* Java 8 runtime minimum for `openapi-generator`, see [API client generation](#api-clientgeneration)

Running locally in dev mode
* Install dependencies `npm ci --include=dev`
* Generate API client `npm run client-gen`
* Start dev-server `npm start`
* Frontend served at [http://localhost:3003](http://localhost:3003) and it will by default 
  expect [backend](../citybikeapp-backend/) to be available at `http://localhost:8080`

## API client generation

Frontend depends on [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator) to create 
a `typescript-axios` client that contains all the backend API responses and their types. This depends on having access 
to the backend OpenAPI specification file. In this monorepo, we can it access directly from backend directory during 
development.

In image build process for local docker-compose setup (see [Dockerfile.frontend](../docker/Dockerfile.frontend)), the 
code is generated using a separate `openapitools/openapi-generator-cli` image.

## NPM commands

* `build` create production build to `build/`
* `start` run development mode (`webpack-dev-server`)
* `lint` run `eslint`
* `client-gen` generate API client using backend OpenAPI specification
* `analyze` create source map visualization to `sourcemap.html` (see [source-map-explorer](https://github.com/danvk/source-map-explorer))

## Environment variables

You can create a `.env` file (see [`.env.example`](.env.example)) to set these in development.

| Environment variable |                               | Default                                            | Required | Example |
|----------------------|-------------------------------|----------------------------------------------------|----------|---------|
| `DEV_PORT`           | Development server port       | `3003`                                             |          |         |
| `API_BASE_URL`       | API url, absolute or relative | `http://localhost:8080` in dev, `/api` in prod     |          |         |

## Technologies

Mainly using components I'm at least somewhat familiar with.

* Main library: React
* Language: TypeScript
* Tooling: Nothing. For a real world project, something like CRA or Vite would probably make more sense.
* Build: Webpack with ts-loader
* State: Manual using React, considered Redux but seemed like a heavyweight option for this.
  * Not too much in global state ATM. Just the all stations listing with limited information (id, Finnish name).
* Code quality / static analysis: ESLint
* Component library / styling: Chakra UI, using Emotion
