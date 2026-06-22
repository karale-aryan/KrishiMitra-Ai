package com.krishimitra.translation;

import com.krishimitra.shared.dto.ApiResponse;
import com.krishimitra.translation.dto.TranslationRequest;
import com.krishimitra.translation.dto.TranslationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    public ResponseEntity<ApiResponse<TranslationResponse>> translate(
            @Valid @RequestBody TranslationRequest request) {

        log.info("Request received to translate text. Source: {}, Target: {}", 
                request.getSourceLanguage(), request.getTargetLanguage());
        TranslationResponse response = translationService.translate(request);
        return ResponseEntity.ok(ApiResponse.success("Text translated successfully", response));
    }
}
