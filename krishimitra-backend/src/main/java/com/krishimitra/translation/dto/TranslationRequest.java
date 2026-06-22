package com.krishimitra.translation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the translation endpoint.
 * Supported languages: en (English), hi (Hindi), mr (Marathi), te (Telugu), kn (Kannada).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest {

    @NotBlank(message = "Source text must not be blank")
    private String sourceText;

    @NotBlank(message = "Source language must not be blank")
    @Pattern(regexp = "^(en|hi|mr|te|kn)$", message = "Source language must be one of: en, hi, mr, te, kn")
    private String sourceLanguage;

    @NotBlank(message = "Target language must not be blank")
    @Pattern(regexp = "^(en|hi|mr|te|kn)$", message = "Target language must be one of: en, hi, mr, te, kn")
    private String targetLanguage;
}
