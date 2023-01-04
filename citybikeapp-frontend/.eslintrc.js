module.exports = {
  env: {
    browser: true,
    es2021: true
  },
  extends: [
    "plugin:react/recommended",
    "standard-with-typescript",
    "plugin:import/recommended",
    "plugin:import/typescript"
  ],
  overrides: [],
  parserOptions: {
    project: "./tsconfig.json",
    ecmaVersion: "latest",
    sourceType: "module"
  },
  plugins: [
    "react"
  ],
  settings: {
    react: {
      version: "detect"
    },
    "import/parsers": {
      "@typescript-eslint/parser": [
        ".ts",
        ".tsx"
      ]
    },
    "import/resolver": {
      typescript: true,
      node: true
    }
  },
  rules: {
    quotes: "off",
    "@typescript-eslint/quotes": [
      "error",
      "double",
      { avoidEscape: true }
    ],
    semi: [
      "error",
      "always"
    ],
    "@typescript-eslint/semi": [
      "error",
      "always"
    ],
    "comma-dangle": [
      "error",
      "only-multiline"
    ],
    "@typescript-eslint/explicit-function-return-type": 0,
    // generic type parameter indent
    "@typescript-eslint/indent": [
      "error",
      2,
      {
        ignoredNodes: [
          "TSTypeParameterInstantiation"
        ]
      }
    ],
    "import/order": [
      "error",
      {
        groups: [
          "builtin",
          "external",
          "internal",
          "parent",
          "sibling",
          "index",
          "object",
          "type"
        ]
      }
    ]
  }
}
