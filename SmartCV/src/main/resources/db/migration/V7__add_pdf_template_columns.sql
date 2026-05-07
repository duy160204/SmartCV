-- Migration: Add pdf_html and pdf_css columns to templates table
-- Purpose: Separate PDF-specific template from browser-preview template
-- pdf_html uses TABLE layout (no flex/grid) — compatible with openhtmltopdf
-- pdf_css uses A4 @page rules and NotoSans font
-- Date: 2026-04-21

ALTER TABLE templates
    ADD COLUMN IF NOT EXISTS pdf_html LONGTEXT NULL COMMENT 'Table-based HTML for PDF export (no flex/grid)',
    ADD COLUMN IF NOT EXISTS pdf_css  LONGTEXT NULL COMMENT 'A4 CSS for PDF export (NotoSans, @page A4)';
