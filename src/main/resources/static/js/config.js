/**
 * LearnLoop Frontend Runtime Configuration
 *
 * When deploying the frontend to Vercel independently from the Spring Boot backend on Render,
 * you can optionally specify the backend base URL below, OR use Vercel's automatic rewrites in vercel.json.
 *
 * Example:
 *   window.LEARNLOOP_API_BASE = 'https://learnloop-backend.onrender.com';
 *
 * If left empty (default), API requests default to '/api/v1', which works automatically when:
 * 1. The Spring Boot backend serves the frontend directly, OR
 * 2. Vercel is configured with rewrites in vercel.json.
 */
window.LEARNLOOP_API_BASE = window.LEARNLOOP_API_BASE || '';
