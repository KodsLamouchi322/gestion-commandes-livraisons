package com.gestion.commandes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Nom obligatoire")
    @Size(max = 100, message = "Nom trop long")
    private String nom;

    @Size(max = 100, message = "Prénom trop long")
    private String prenom;

    @NotBlank(message = "Email obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @Size(max = 20, message = "Téléphone trop long")
    private String telephone;

    @NotBlank(message = "Adresse obligatoire")
    @Size(max = 255, message = "Adresse trop longue")
    private String adresse;

    @NotBlank(message = "Mot de passe obligatoire")
    @Size(min = 6, message = "Mot de passe trop court (min 6 caractères)")
    private String motDePasse;
}
