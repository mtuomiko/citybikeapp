# City Bike App backend

### API client generation

Frontend uses OpenAPI generator to create a `typescript-axios` client that contains all the API responses and their 
types. This depends on having access to the backend OpenAPI specification file. In this monorepo, we can it access 
directly from backend directory.

### NPM commands

* `build` create production build to `build/`
* `start` run development mode (`webpack-dev-server`)
* `lint` run `eslint`
* `client-gen` generate API client using backend OpenAPI specification

### Environment variables

You can create a `.env` file (see [`.env.example`](.env.example)) to set these in development.

| Environment variable |                               | Default                                            | Required | Example |
|----------------------|-------------------------------|----------------------------------------------------|----------|---------|
| `DEV_PORT`           | Development server port       | `3003`                                             |          |         |
| `API_BASE_URL`       | API url, absolute or relative | `http://localhost:8080` in dev, `/api` in prod     |          |         |
