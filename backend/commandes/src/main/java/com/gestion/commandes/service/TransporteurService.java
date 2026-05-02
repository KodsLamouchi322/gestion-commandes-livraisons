package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.TransporteurDTO;
import com.gestion.commandes.entity.Transporteur;
import com.gestion.commandes.repository.LivraisonRepository;
import com.gestion.commandes.repository.TransporteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Transporteur - Gère la logique métier des transporteurs
 */
@Service
public class TransporteurService {

    @Autowired
    private TransporteurRepository rep;

    @Autowired
    private LivraisonRepository livraisonRepository;

    // Converter : entité → DTO
    @Autowired
    private EntityConverter converter;

    // ============================================================
    // LECTURE
    // ============================================================

    /**
     * Récupère tous les transporteurs sous forme de DTOs
     */
    public List<TransporteurDTO> chercherTout() {
        return rep.findAll()
                  .stream()
                  .map(converter::toTransporteurDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère un transporteur par son ID (retourne un DTO)
     */
    public TransporteurDTO chercherParId(Integer id) {
        Transporteur t = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transporteur introuvable"));
        return converter.toTransporteurDTO(t);
    }

    /**
     * Recherche des transporteurs par nom (retourne des DTOs)
     */
    public List<TransporteurDTO> chercherParNom(String nom) {
        return rep.findByNomStartingWith(nom)
                  .stream()
                  .map(converter::toTransporteurDTO)
                  .collect(Collectors.toList());
    }

    // ============================================================
    // CRÉATION
    // ============================================================

    /**
     * Ajoute un nouveau transporteur - retourne un DTO
     */
    @Transactional
    public TransporteurDTO ajouter(Transporteur t) {
        validerChamps(t);
        t.setNom(t.getNom().trim());
        t.setTelephone(t.getTelephone().trim());
        if (t.getEmail() != null) {
            t.setEmail(t.getEmail().trim().toLowerCase());
        }
        Transporteur saved = rep.save(t);
        return converter.toTransporteurDTO(saved);
    }

    // ============================================================
    // MODIFICATION
    // ============================================================

    /**
     * Modifie un transporteur existant - retourne un DTO
     */
    @Transactional
    public TransporteurDTO update(Integer id, Transporteur t) {
        Transporteur existant = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transporteur introuvable"));
        existant.setNom(t.getNom().trim());
        existant.setTelephone(t.getTelephone().trim());
        existant.setEmail(t.getEmail() != null ? t.getEmail().trim().toLowerCase() : null);
        existant.setNote(t.getNote());
        Transporteur saved = rep.save(existant);
        return converter.toTransporteurDTO(saved);
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    /**
     * Supprime un transporteur
     * Interdit si le transporteur est associé à des livraisons
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transporteur introuvable");
        }
        if (livraisonRepository.countByTransporteurId(id) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Suppression impossible: ce transporteur est lie a des livraisons");
        }
        rep.deleteById(id);
    }

    // ============================================================
    // VALIDATION INTERNE
    // ============================================================

    private void validerChamps(Transporteur t) {
        if (t.getNom() == null || t.getNom().trim().isEmpty()
                || t.getTelephone() == null || t.getTelephone().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nom et telephone sont obligatoires");
        }
        if (t.getEmail() != null && !t.getEmail().trim().isEmpty()
                && !t.getEmail().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Email transporteur invalide");
        }
    }
}
