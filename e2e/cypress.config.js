const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      return config
    },
    baseUrl: `http://localhost:${process.env.PORT || 3003}`
  },
});
