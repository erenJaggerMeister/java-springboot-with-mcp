-- Update default gemini_model from unsupported gemini-2.5-flash to gemini-1.5-flash
UPDATE msm_ai.ai_config
SET config_value = 'gemini-1.5-flash', updated_at = CURRENT_TIMESTAMP
WHERE config_key = 'gemini_model' AND config_value = 'gemini-2.5-flash';
