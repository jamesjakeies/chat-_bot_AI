import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#17211b',
        moss: '#506a4f',
        oat: '#f5efe1',
        clay: '#b86f52',
        lagoon: '#426b74',
      },
      boxShadow: {
        soft: '0 22px 70px rgba(23, 33, 27, 0.13)',
      },
      fontFamily: {
        display: ['Georgia', 'ui-serif', 'serif'],
        body: ['"Avenir Next"', '"Segoe UI"', 'sans-serif'],
      },
    },
  },
  plugins: [],
};

export default config;
