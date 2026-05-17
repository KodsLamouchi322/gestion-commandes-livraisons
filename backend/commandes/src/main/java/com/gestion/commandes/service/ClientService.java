package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.ClientDTO;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Role;
import com.gestion.commandes.repository.ClientRepository;
import com.gestion.commandes.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Client - Gère la logique métier des clients
 *
 * Les mots de passe sont hashés avec BCrypt via PasswordEncoder (@Bean dans SecurityConfig).
 * Le motDePasse n'est JAMAIS retourné au frontend (utilisation de ClientDTO).
 */
@SuppressWarnings("null")
@Service
public class ClientService {

    @Autowired
    private ClientRepository rep;

    // PasswordEncoder est un @Bean déclaré dans SecurityConfig
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommandeRepository commandeRepository;

    // Converter : entité → DTO (sans mot de passe)
    @Autowired
    private EntityConverter converter;

    // ============================================================
    // LECTURE
    // ============================================================

    /**
     * Récupère tous les clients sous forme de DTOs (sans mot de passe)
     */
    public List<ClientDTO> chercherTout() {
        return rep.findAll()
                  .stream()
                  .map(converter::toClientDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère un client par son ID (retourne un DTO sans mot de passe)
     */
    public ClientDTO chercherParId(Integer id) {
        Client client = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client introuvable avec l'ID : " + id));
        return converter.toClientDTO(client);
    }

    /**
     * Récupère un client par son email (retourne un DTO sans mot de passe)
     */
    public ClientDTO chercherParEmail(String email) {
        Client client = rep.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client introuvable avec l'email : " + email));
        return converter.toClientDTO(client);
    }

    /**
     * Recherche des clients par nom (retourne des DTOs)
     */
    public List<ClientDTO> chercherParNom(String nom) {
        return rep.findByNomStartingWith(nom)
                  .stream()
                  .map(converter::toClientDTO)
                  .collect(Collectors.toList());
    }

    // ============================================================
    // CRÉATION
    // ============================================================

    /**
     * Ajoute un nouveau client (inscription)
     * Le mot de passe est hashé avec BCrypt avant sauvegarde
     * Retourne un DTO sans mot de passe
     */
    @Transactional
    public ClientDTO ajouter(Client c) {
        if (c.getNom() == null || c.getNom().trim().isEmpty()
                || c.getEmail() == null || c.getEmail().trim().isEmpty()
                || c.getAdresse() == null || c.getAdresse().trim().isEmpty()
                || c.getMotDePasse() == null || c.getMotDePasse().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nom, email, adresse et mot de passe sont obligatoires");
        }

        String email = c.getEmail().trim().toLowerCase();
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email client invalide");
        }

        if (rep.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un client avec cet email existe deja");
        }

        if (c.getRole() == null) {
            c.setRole(Role.CLIENT);
        }

        c.setNom(c.getNom().trim());
        c.setEmail(email);
        c.setAdresse(c.getAdresse().trim());
        // Hash du mot de passe via BCrypt (PasswordEncoder est un @Bean)
        c.setMotDePasse(passwordEncoder.encode(c.getMotDePasse()));

        Client saved = rep.save(c);
        return converter.toClientDTO(saved);
    }

    // ============================================================
    // MODIFICATION
    // ============================================================

    /**
     * Modifie un client existant - retourne un DTO sans mot de passe
     * Si un nouveau mot de passe est fourni, il est haché avant sauvegarde
     */
    @Transactional
    public ClientDTO update(Integer id, Client c) {
        if (c.getNom() == null || c.getNom().trim().isEmpty()
                || c.getEmail() == null || c.getEmail().trim().isEmpty()
                || c.getAdresse() == null || c.getAdresse().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nom, email et adresse sont obligatoires");
        }

        String email = c.getEmail().trim().toLowerCase();
        if (!email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email client invalide");
        }

        Client existant = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client introuvable avec l'ID : " + id));

        if (!existant.getEmail().equalsIgnoreCase(email) && rep.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cet email est deja utilise par un autre client");
        }

        existant.setNom(c.getNom().trim());
        existant.setPrenom(c.getPrenom());
        existant.setEmail(email);
        existant.setTelephone(c.getTelephone());
        existant.setAdresse(c.getAdresse().trim());

        // Mise à jour du mot de passe uniquement si fourni
        if (c.getMotDePasse() != null && !c.getMotDePasse().isEmpty()) {
            existant.setMotDePasse(passwordEncoder.encode(c.getMotDePasse()));
        }

        if (c.getRole() != null) {
            existant.setRole(c.getRole());
        }

        Client saved = rep.save(existant);
        return converter.toClientDTO(saved);
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    /**
     * Supprime un client
     * Interdit si : admin, ou si le client a des commandes
     */
    @Transactional
    public void delete(Integer id) {
        Client client = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Client introuvable avec l'ID : " + id));

        if (client.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Suppression d'un admin interdite");
        }

        if (commandeRepository.countByClientId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Suppression impossible: ce client a des commandes");
        }

        rep.deleteById(id);
    }
}
