package com.gestion.commandes.config;

import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Role;
import com.gestion.commandes.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@gestion.local}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @PostConstruct
    public void creerAdminParDefaut() {
        if (clientRepository.existsByEmail(adminEmail)) {
            return;
        }
        Client admin = new Client();
        admin.setNom("Administrateur");
        admin.setEmail(adminEmail);
        admin.setAdresse("Siège social");
        admin.setMotDePasse(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        clientRepository.save(admin);
    }
}
