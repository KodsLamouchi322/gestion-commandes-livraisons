package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Avis;
import com.gestion.commandes.service.AvisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    @Autowired
    private AvisService service;

    @GetMapping
    public ResponseEntity<List<Avis>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avis> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<Avis>> getByProduit(@PathVariable Integer produitId) {
        return ResponseEntity.ok(service.chercherParProduit(produitId));
    }

    @GetMapping("/produit/{produitId}/moyenne")
    public ResponseEntity<Map<String, Object>> getNoteMoyenne(@PathVariable Integer produitId) {
        Double moyenne = service.getNoteMoyenne(produitId);
        List<Avis> avis = service.chercherParProduit(produitId);
        return ResponseEntity.ok(Map.of(
            "noteMoyenne", moyenne,
            "count", avis.size()
        ));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Avis>> getByClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(service.chercherParClient(clientId));
    }

    @PostMapping
    public ResponseEntity<Avis> create(@RequestBody Avis a) {
        return ResponseEntity.ok(service.ajouter(a));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Avis supprimé avec succès");
    }
}
