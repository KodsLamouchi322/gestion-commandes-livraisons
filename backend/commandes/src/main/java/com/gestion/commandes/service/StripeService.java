package com.gestion.commandes.service;

import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.entity.Paiement;
import com.gestion.commandes.repository.CommandeRepository;
import com.gestion.commandes.repository.PaiementRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class StripeService {

    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isEmpty()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    /**
     * Crée une session de paiement Stripe Checkout
     */
    public String createCheckoutSession(Integer commandeId) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe n'est pas configuré. Veuillez configurer stripe.secret.key");
        }

        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Commande introuvable"));

        if (commande.getStatut() == Commande.StatutCommande.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Impossible de payer une commande annulée");
        }

        // Vérifier si un paiement existe déjà
        if (paiementRepository.findByCommandeId(commandeId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un paiement existe déjà pour cette commande");
        }

        // Convertir le montant en centimes (Stripe utilise les plus petites unités)
        long montantEnCentimes = Math.round(commande.getMontantTotal() * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/paiement/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/paiement/cancel?commande_id=" + commandeId)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(montantEnCentimes)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Commande #" + commandeId)
                                                                .setDescription("Paiement de la commande")
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("commande_id", commandeId.toString())
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Vérifie le statut d'une session Stripe et crée le paiement si validé
     */
    public Paiement verifySession(String sessionId) throws StripeException {
        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Stripe n'est pas configuré");
        }

        Session session = Session.retrieve(sessionId);

        if (!"paid".equals(session.getPaymentStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le paiement n'a pas été validé");
        }

        Integer commandeId = Integer.parseInt(session.getMetadata().get("commande_id"));
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Commande introuvable"));

        // Créer le paiement
        Paiement paiement = new Paiement();
        paiement.setCommande(commande);
        paiement.setMontant(commande.getMontantTotal());
        paiement.setMethodePaiement(Paiement.MethodePaiement.CARTE);
        paiement.setStatut(Paiement.StatutPaiement.VALIDE);
        paiement.setDatePaiement(LocalDateTime.now());

        Paiement saved = paiementRepository.save(paiement);

        // Valider la commande
        commande.setStatut(Commande.StatutCommande.VALIDEE);
        commandeRepository.save(commande);

        return saved;
    }
}
