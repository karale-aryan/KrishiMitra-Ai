-- =====================================================
-- KrishiMitra AI - Fix preferred_language column width
-- Flyway Migration V4
-- The column was VARCHAR(5) intended for language codes (e.g. 'en', 'hi'),
-- but the frontend sends full language names (e.g. 'English', 'Hindi').
-- Widen to VARCHAR(50) to support both.
-- =====================================================

ALTER TABLE users ALTER COLUMN preferred_language TYPE VARCHAR(50);
ALTER TABLE advisory_logs ALTER COLUMN query_language TYPE VARCHAR(50);
ALTER TABLE advisory_logs ALTER COLUMN response_language TYPE VARCHAR(50);
