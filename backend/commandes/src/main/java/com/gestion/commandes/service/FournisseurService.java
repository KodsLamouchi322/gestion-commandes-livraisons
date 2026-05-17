package com.gestion.commandes.service;

import com.gestion.commandes.entity.Fournisseur;
import com.gestion.commandes.repository.BonCommandeRepository;
import com.gestion.commandes.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SuppressWarnings("null")
@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository rep;
    @Autowired
    private BonCommandeRepository bonCommandeRepository;

    public List<Fournisseur> chercherTout() {
        return rep.findAll();
    }

    public Fournisseur chercherParId(Integer id) {
        return rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Fournisseur introuvable"));
    }

    public List<Fournisseur> chercherParNom(String nom) {
        return rep.findByNomStartingWith(nom);
    }

    @Transactional
    public Fournisseur ajouter(Fournisseur f) {
        validerChamps(f);
        String email = f.getEmail().trim().toLowerCase();
        if (rep.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Un fournisseur avec cet email existe déjà");
        }
        f.setNom(f.getNom().trim());
        f.setEmail(email);
        f.setTelephone(f.getTelephone().trim());
        f.setAdresse(f.getAdresse().trim());
        return rep.save(f);
    }

    @Transactional
    public Fournisseur update(Integer id, Fournisseur f) {
        validerChamps(f);
        Fournisseur existant = chercherParId(id);
        String email = f.getEmail().trim().toLowerCase();
        if (!existant.getEmail().equalsIgnoreCase(email) && rep.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un fournisseur avec cet email existe déjà");
        }
        existant.setNom(f.getNom().trim());
        existant.setEmail(email);
        existant.setTelephone(f.getTelephone().trim());
        existant.setAdresse(f.getAdresse().trim());
        return rep.save(existant);
    }

    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fournisseur introuvable");
        }
        if (bonCommandeRepository.countByFournisseurId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Suppression impossible: des bons de commande existent pour ce fournisseur");
        }
        rep.deleteById(id);
    }

    private void validerChamps(Fournisseur f) {
        if (f.getNom() == null || f.getNom().trim().isEmpty()
                || f.getEmail() == null || f.getEmail().trim().isEmpty()
                || f.getTelephone() == null || f.getTelephone().trim().isEmpty()
                || f.getAdresse() == null || f.getAdresse().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tous les champs fournisseur sont obligatoires");
        }
        if (!f.getEmail().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email fournisseur invalide");
        }
    }
}
