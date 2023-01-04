# City Bike App backend

### NPM commands

* `build` create production build to `build/`
* `start` run development mode (`webpack-dev-server`)
* `lint` run `eslint`

### Environment variables

You can create a `.env` file (see [`.env.example`](.env.example)) to set these in development.

| Environment variable |                               | Default                                            | Required | Example |
|----------------------|-------------------------------|----------------------------------------------------|----------|---------|
| `DEV_PORT`           | Development server port       | `3003`                                             |          |         |
| `API_BASE_URL`       | API url, absolute or relative | `http://localhost:8080/api` in dev, `/api` in prod |          |         |
