package com.gestion.commandes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // intercepte toutes les exceptions de tous les controllers
public class GlobalExceptionHandler {

    // Gestion des erreurs de validation (@NotBlank, @Email, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();

        // Pour chaque champ invalide, on récupère le message d'erreur
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String champ = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            erreurs.put(champ, message);
        });

        Map<String, Object> reponse = new HashMap<>();
        reponse.put("timestamp", LocalDateTime.now());
        reponse.put("statut", HttpStatus.BAD_REQUEST.value());
        reponse.put("erreurs", erreurs);

        return ResponseEntity.badRequest().body(reponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("timestamp", LocalDateTime.now());
        reponse.put("statut", HttpStatus.FORBIDDEN.value());
        reponse.put("message", "Accès refusé : droits insuffisants");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(reponse);
    }

    // Gestion des RuntimeException (ex: "Client introuvable")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> reponse = new HashMap<>();
        reponse.put("timestamp", LocalDateTime.now());
        reponse.put("statut", HttpStatus.BAD_REQUEST.value());
        reponse.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(reponse);
    }
}
