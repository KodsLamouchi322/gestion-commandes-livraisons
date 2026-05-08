package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Fournisseur;
import com.gestion.commandes.service.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService service;

    @GetMapping
    public ResponseEntity<List<Fournisseur>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fournisseur> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<Fournisseur>> rechercherParNom(@RequestParam String nom) {
        return ResponseEntity.ok(service.chercherParNom(nom));
    }

    @PostMapping
    public ResponseEntity<Fournisseur> create(@RequestBody Fournisseur f) {
        return ResponseEntity.ok(service.ajouter(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fournisseur> update(@PathVariable Integer id, @RequestBody Fournisseur f) {
        return ResponseEntity.ok(service.update(id, f));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Fournisseur supprimé avec succès");
    }
}
