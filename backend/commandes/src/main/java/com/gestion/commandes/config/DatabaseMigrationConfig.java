package com.gestion.commandes.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Corrections de schéma que Hibernate ddl-auto=update ne gère pas
 * (ex: modification de colonnes ENUM existantes).
 */
@Component
@RequiredArgsConstructor
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbc;

    @PostConstruct
    public void migrer() {
        try {
            // Élargir la colonne mode de paiements en VARCHAR pour accepter STRIPE
            jdbc.execute("ALTER TABLE paiements MODIFY COLUMN mode VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {
            // Déjà migré ou colonne déjà correcte
        }

        try {
            // Élargir statut paiements
            jdbc.execute("ALTER TABLE paiements MODIFY COLUMN statut VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {}

        try {
            // Élargir statut commandes
            jdbc.execute("ALTER TABLE commandes MODIFY COLUMN statut VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {}

        try {
            // Élargir statut livraisons
            jdbc.execute("ALTER TABLE livraisons MODIFY COLUMN statut VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {}

        try {
            // Élargir statut bons_commande
            jdbc.execute("ALTER TABLE bons_commande MODIFY COLUMN statut VARCHAR(30) NOT NULL");
        } catch (Exception ignored) {}
    }
}
