/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#D4A574',
          dark: '#B8935E',
          light: '#E8D4B8',
        },
        surface: {
          DEFAULT: '#FFFFFF',
          secondary: '#FFF8F0',
        },
        background: '#FAFAF8',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'sans-serif'],
      },
      boxShadow: {
        'sm': '0 1px 3px rgba(0,0,0,0.08)',
        'md': '0 4px 12px rgba(0,0,0,0.1)',
        'lg': '0 10px 40px rgba(0,0,0,0.12)',
      },
      borderRadius: {
        'sm': '12px',
        'md': '16px',
        'lg': '24px',
      },
    },
  },
  plugins: [],
}
