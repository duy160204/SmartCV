import { marked } from 'marked';
import DOMPurify from 'dompurify';

export const processAiAnswer = async (raw: any): Promise<string> => {
    if (!raw) return '';

    if (typeof raw !== "string") {
        console.error("Invalid markdown input:", raw);
        return "";
    }

    // Convert markdown to raw HTML string
    const rawHtml = await marked.parse(raw, { async: true });

    // Sanitize with strict rules
    const safeHtml = DOMPurify.sanitize(rawHtml, {
        ALLOWED_TAGS: [
            'p', 'br', 'strong', 'b', 'em', 'i',
            'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4',
            'code', 'pre', 'blockquote'
        ],
        ALLOWED_ATTR: [], // No attributes allowed by default (no src, no href, no style, no arbitrary attributes)
        ALLOW_DATA_ATTR: false,
    });

    return safeHtml;
};
