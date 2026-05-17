package com.gestion.commandes.controller;

import com.gestion.commandes.dto.AuthResponse;
import com.gestion.commandes.dto.ClientDTO;
import com.gestion.commandes.dto.LoginRequest;
import com.gestion.commandes.dto.RegisterRequest;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.repository.ClientRepository;
import com.gestion.commandes.security.JwtService;
import com.gestion.commandes.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Auth - Gestion de l'authentification JWT
 */
@SuppressWarnings("null")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/register - Inscription d'un nouveau client
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Créer un client à partir de la requête
        Client client = new Client();
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom() != null ? request.getPrenom() : "");
        client.setEmail(request.getEmail());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse() != null ? request.getAdresse() : "");
        client.setMotDePasse(request.getMotDePasse());

        ClientDTO savedDTO = clientService.ajouter(client);
        Client savedEntity = clientRepository.findById(savedDTO.getId()).orElseThrow();
        String token = jwtService.generateToken(savedEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, savedDTO.getId(), savedDTO.getEmail(), 
                      savedDTO.getNom(), savedDTO.getPrenom(), savedDTO.getRole().name()));
    }

    /**
     * POST /api/auth/login - Connexion avec JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Rechercher le client par email
        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // Vérifier le mot de passe hashé
        if (!passwordEncoder.matches(request.getMotDePasse(), client.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(client);
        return ResponseEntity.ok(new AuthResponse(token, client.getId(), client.getEmail(), 
                client.getNom(), client.getPrenom(), client.getRole().name()));
    }
}
