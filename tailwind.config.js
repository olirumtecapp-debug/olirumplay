/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cyber: {
          bg: '#060B14',
          card: '#0A111E',
          surface: '#0F1A2D',
          border: '#1A2942',
          cyan: '#00F5D4',
          cyanGlow: '#00D2FF',
          purple: '#7040F0',
          purpleGlow: '#8B5CF6',
          pink: '#F72585'
        }
      },
      boxShadow: {
        'neon-cyan': '0 0 20px rgba(0, 245, 212, 0.35)',
        'neon-purple': '0 0 20px rgba(112, 64, 240, 0.35)',
        'neon-pink': '0 0 20px rgba(247, 37, 133, 0.35)'
      }
    },
  },
  plugins: [],
}
