# City Bike App frontend

## Technologies

* Main library: React
* Language: TypeScript
* Tooling: Nothing, manual setup just for practice. For a real world project, something like CRA or Vite would probably 
make more sense.
* Build: Webpack with ts-loader
* State: Manual using React, considered Redux but seemed like a heavyweight option for this.
* Code quality / static analysis: ESLint
* Component library / styling: Chakra UI, using Emotion

## Getting started

#### Requirements

* Node/npm. Developed on v18 so should work at least on that.

## API client generation

Frontend uses OpenAPI generator to create a `typescript-axios` client that contains all the API responses and their 
types. This depends on having access to the backend OpenAPI specification file. In this monorepo, we can it access 
directly from backend directory.

## NPM commands

* `build` create production build to `build/`
* `start` run development mode (`webpack-dev-server`)
* `lint` run `eslint`
* `client-gen` generate API client using backend OpenAPI specification

## Environment variables

You can create a `.env` file (see [`.env.example`](.env.example)) to set these in development.

| Environment variable |                               | Default                                            | Required | Example |
|----------------------|-------------------------------|----------------------------------------------------|----------|---------|
| `DEV_PORT`           | Development server port       | `3003`                                             |          |         |
| `API_BASE_URL`       | API url, absolute or relative | `http://localhost:8080` in dev, `/api` in prod     |          |         |
