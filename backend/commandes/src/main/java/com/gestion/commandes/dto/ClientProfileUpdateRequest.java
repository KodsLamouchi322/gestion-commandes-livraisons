package com.gestion.commandes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientProfileUpdateRequest {
    @Size(max = 100, message = "Nom trop long")
    private String nom;

    @Size(max = 100, message = "Prénom trop long")
    private String prenom;

    @Email(message = "Email invalide")
    private String email;

    @Size(max = 20, message = "Téléphone trop long")
    private String telephone;

    @Size(max = 255, message = "Adresse trop longue")
    private String adresse;

    @Size(min = 6, message = "Mot de passe trop court (min 6 caractères)")
    private String motDePasse;
}
