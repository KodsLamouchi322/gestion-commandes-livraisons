package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Stock - Gère les requêtes HTTP pour la gestion du stock
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    /**
     * GET /api/stock/faible - Récupère les produits avec un stock faible (< 10 unités)
     */
    @GetMapping("/faible")
    public ResponseEntity<List<Produit>> getProduitsStockFaible() {
        List<Produit> produits = stockService.produitsStockFaible();
        return ResponseEntity.ok(produits);
    }

    /**
     * GET /api/stock/produit/{id} - Récupère le niveau de stock d'un produit
     */
    @GetMapping("/produit/{id}")
    public ResponseEntity<Map<String, Object>> getStockProduit(@PathVariable Integer id) {
        // Cette méthode retourne le stock d'un produit spécifique
        // On utilise le service pour vérifier si le produit existe et récupérer son stock
        stockService.estStockSuffisant(id, 0);
        
        // Récupérer le produit via le repository pour obtenir le stock actuel
        // Note: Pour l'instant, on retourne juste une confirmation que le produit existe
        Map<String, Object> response = new HashMap<>();
        response.put("produitId", id);
        response.put("message", "Utilisez GET /api/produits/{id} pour obtenir les détails complets du produit incluant le stock");
        
        return ResponseEntity.ok(response);
    }
}
