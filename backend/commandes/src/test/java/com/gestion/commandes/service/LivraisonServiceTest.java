package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.LivraisonDTO;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.entity.Livraison;
import com.gestion.commandes.entity.Transporteur;
import com.gestion.commandes.repository.CommandeRepository;
import com.gestion.commandes.repository.LivraisonRepository;
import com.gestion.commandes.repository.TransporteurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour LivraisonService
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class LivraisonServiceTest {

    @Mock
    private LivraisonRepository livraisonRepository;

    @Mock
    private CommandeRepository commandeRepository;

    @Mock
    private TransporteurRepository transporteurRepository;

    @Mock
    private EntityConverter converter;

    @InjectMocks
    private LivraisonService livraisonService;

    private Commande commandeValidee;
    private Transporteur transporteur;
    private Livraison livraison;
    private LivraisonDTO livraisonDTO;

    @BeforeEach
    void setUp() {
        // Créer une commande validée
        commandeValidee = new Commande();
        commandeValidee.setId(1);
        commandeValidee.setStatut(Commande.StatutCommande.VALIDEE);
        commandeValidee.setAdresseLivraison("123 Rue Test, Paris");
        commandeValidee.setDateCommande(LocalDateTime.now());
        commandeValidee.setMontantTotal(100.0);

        // Créer un transporteur
        transporteur = new Transporteur();
        transporteur.setId(1);
        transporteur.setNom("DHL");
        transporteur.setTelephone("0123456789");

        // Créer une livraison
        livraison = new Livraison();
        livraison.setId(1);
        livraison.setCommande(commandeValidee);
        livraison.setAdresse("123 Rue Test, Paris");
        livraison.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        livraison.setCout(10.0);
        livraison.setDateLivraison(LocalDateTime.now().plusDays(3));
        livraison.setTransporteur(transporteur);

        // Créer un DTO
        livraisonDTO = new LivraisonDTO();
        livraisonDTO.setId(1);
        livraisonDTO.setCommandeId(1);
        livraisonDTO.setAdresse("123 Rue Test, Paris");
        livraisonDTO.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        livraisonDTO.setCout(10.0);
        livraisonDTO.setTransporteurId(1);
        livraisonDTO.setTransporteurNom("DHL");
    }

    @Test
    void testCreerDepuisCommande_ValideeOrder_Success() {
        // Arrange
        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeValidee));
        when(livraisonRepository.existsByCommandeId(1)).thenReturn(false);
        when(transporteurRepository.findById(1)).thenReturn(Optional.of(transporteur));
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(livraison);
        when(converter.toLivraisonDTO(any(Livraison.class))).thenReturn(livraisonDTO);

        // Act
        LivraisonDTO result = livraisonService.creerDepuisCommande(1, 10.0, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCommandeId());
        assertEquals("123 Rue Test, Paris", result.getAdresse());
        assertEquals(Livraison.StatutLivraison.EN_PREPARATION, result.getStatut());
        assertEquals(10.0, result.getCout());
        assertEquals(1, result.getTransporteurId());

        verify(commandeRepository).findById(1);
        verify(livraisonRepository).existsByCommandeId(1);
        verify(transporteurRepository).findById(1);
        verify(livraisonRepository).save(any(Livraison.class));
        verify(converter).toLivraisonDTO(any(Livraison.class));
    }

    @Test
    void testCreerDepuisCommande_WithoutTransporteur_Success() {
        // Arrange
        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeValidee));
        when(livraisonRepository.existsByCommandeId(1)).thenReturn(false);
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(livraison);
        when(converter.toLivraisonDTO(any(Livraison.class))).thenReturn(livraisonDTO);

        // Act
        LivraisonDTO result = livraisonService.creerDepuisCommande(1, 10.0, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCommandeId());
        assertEquals(Livraison.StatutLivraison.EN_PREPARATION, result.getStatut());

        verify(commandeRepository).findById(1);
        verify(livraisonRepository).existsByCommandeId(1);
        verify(transporteurRepository, never()).findById(any());
        verify(livraisonRepository).save(any(Livraison.class));
    }

    @Test
    void testCreerDepuisCommande_DefaultCost_Success() {
        // Arrange
        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeValidee));
        when(livraisonRepository.existsByCommandeId(1)).thenReturn(false);
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(livraison);
        when(converter.toLivraisonDTO(any(Livraison.class))).thenReturn(livraisonDTO);

        // Act
        LivraisonDTO result = livraisonService.creerDepuisCommande(1, null, null);

        // Assert
        assertNotNull(result);
        verify(livraisonRepository).save(argThat(l -> l.getCout() == 0.0));
    }

    @Test
    void testCreerDepuisCommande_CommandeNotFound_ThrowsException() {
        // Arrange
        when(commandeRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.creerDepuisCommande(999, 10.0, null)
        );

        assertTrue(exception.getMessage().contains("Commande introuvable"));
        verify(commandeRepository).findById(999);
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testCreerDepuisCommande_CommandeNotValidee_ThrowsException() {
        // Arrange
        Commande commandeEnAttente = new Commande();
        commandeEnAttente.setId(1);
        commandeEnAttente.setStatut(Commande.StatutCommande.EN_ATTENTE);
        commandeEnAttente.setAdresseLivraison("123 Rue Test, Paris");

        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeEnAttente));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.creerDepuisCommande(1, 10.0, null)
        );

        assertTrue(exception.getMessage().contains("VALIDEE"));
        verify(commandeRepository).findById(1);
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testCreerDepuisCommande_DuplicateDelivery_ThrowsException() {
        // Arrange
        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeValidee));
        when(livraisonRepository.existsByCommandeId(1)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.creerDepuisCommande(1, 10.0, null)
        );

        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(commandeRepository).findById(1);
        verify(livraisonRepository).existsByCommandeId(1);
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testCreerDepuisCommande_TransporteurNotFound_ThrowsException() {
        // Arrange
        when(commandeRepository.findById(1)).thenReturn(Optional.of(commandeValidee));
        when(livraisonRepository.existsByCommandeId(1)).thenReturn(false);
        when(transporteurRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.creerDepuisCommande(1, 10.0, 999)
        );

        assertTrue(exception.getMessage().contains("Transporteur introuvable"));
        verify(commandeRepository).findById(1);
        verify(livraisonRepository).existsByCommandeId(1);
        verify(transporteurRepository).findById(999);
        verify(livraisonRepository, never()).save(any());
    }

    // ============================================================
    // Tests pour assignerTransporteur
    // ============================================================

    @Test
    void testAssignerTransporteur_EnPreparation_Success() {
        // Arrange
        Livraison livraisonSansTransporteur = new Livraison();
        livraisonSansTransporteur.setId(1);
        livraisonSansTransporteur.setCommande(commandeValidee);
        livraisonSansTransporteur.setAdresse("123 Rue Test, Paris");
        livraisonSansTransporteur.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        livraisonSansTransporteur.setCout(10.0);
        livraisonSansTransporteur.setDateLivraison(LocalDateTime.now().plusDays(3));

        when(livraisonRepository.findById(1)).thenReturn(Optional.of(livraisonSansTransporteur));
        when(transporteurRepository.findById(1)).thenReturn(Optional.of(transporteur));
        when(livraisonRepository.save(any(Livraison.class))).thenReturn(livraison);
        when(converter.toLivraisonDTO(any(Livraison.class))).thenReturn(livraisonDTO);

        // Act
        LivraisonDTO result = livraisonService.assignerTransporteur(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTransporteurId());
        assertEquals("DHL", result.getTransporteurNom());

        verify(livraisonRepository).findById(1);
        verify(transporteurRepository).findById(1);
        verify(livraisonRepository).save(argThat(l -> 
            l.getTransporteur() != null && 
            l.getTransporteur().getId() == 1 &&
            l.getStatut() == Livraison.StatutLivraison.EN_PREPARATION
        ));
        verify(converter).toLivraisonDTO(any(Livraison.class));
    }

    @Test
    void testAssignerTransporteur_LivraisonNotFound_ThrowsException() {
        // Arrange
        when(livraisonRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.assignerTransporteur(999, 1)
        );

        assertTrue(exception.getMessage().contains("Livraison introuvable"));
        verify(livraisonRepository).findById(999);
        verify(transporteurRepository, never()).findById(any());
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testAssignerTransporteur_LivraisonLivree_ThrowsException() {
        // Arrange
        Livraison livraisonLivree = new Livraison();
        livraisonLivree.setId(1);
        livraisonLivree.setCommande(commandeValidee);
        livraisonLivree.setAdresse("123 Rue Test, Paris");
        livraisonLivree.setStatut(Livraison.StatutLivraison.LIVREE);
        livraisonLivree.setCout(10.0);
        livraisonLivree.setDateLivraison(LocalDateTime.now());

        when(livraisonRepository.findById(1)).thenReturn(Optional.of(livraisonLivree));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.assignerTransporteur(1, 1)
        );

        assertTrue(exception.getMessage().contains("en préparation"));
        verify(livraisonRepository).findById(1);
        verify(transporteurRepository, never()).findById(any());
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testAssignerTransporteur_LivraisonEnTransit_ThrowsException() {
        // Arrange
        Livraison livraisonEnTransit = new Livraison();
        livraisonEnTransit.setId(1);
        livraisonEnTransit.setCommande(commandeValidee);
        livraisonEnTransit.setAdresse("123 Rue Test, Paris");
        livraisonEnTransit.setStatut(Livraison.StatutLivraison.EXPEDIEE);
        livraisonEnTransit.setCout(10.0);
        livraisonEnTransit.setDateLivraison(LocalDateTime.now());

        when(livraisonRepository.findById(1)).thenReturn(Optional.of(livraisonEnTransit));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.assignerTransporteur(1, 1)
        );

        assertTrue(exception.getMessage().contains("en préparation"));
        verify(livraisonRepository).findById(1);
        verify(transporteurRepository, never()).findById(any());
        verify(livraisonRepository, never()).save(any());
    }

    @Test
    void testAssignerTransporteur_TransporteurNotFound_ThrowsException() {
        // Arrange
        Livraison livraisonSansTransporteur = new Livraison();
        livraisonSansTransporteur.setId(1);
        livraisonSansTransporteur.setCommande(commandeValidee);
        livraisonSansTransporteur.setAdresse("123 Rue Test, Paris");
        livraisonSansTransporteur.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        livraisonSansTransporteur.setCout(10.0);
        livraisonSansTransporteur.setDateLivraison(LocalDateTime.now().plusDays(3));

        when(livraisonRepository.findById(1)).thenReturn(Optional.of(livraisonSansTransporteur));
        when(transporteurRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> livraisonService.assignerTransporteur(1, 999)
        );

        assertTrue(exception.getMessage().contains("Transporteur introuvable"));
        verify(livraisonRepository).findById(1);
        verify(transporteurRepository).findById(999);
        verify(livraisonRepository, never()).save(any());
    }
}
