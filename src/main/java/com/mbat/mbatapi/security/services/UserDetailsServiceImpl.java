package com.mbat.mbatapi.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.repository.UserRepository;

/**
 * Implémentation du service de détails utilisateur pour la gestion de l'authentification.
 * Cette classe permet de charger les détails d'un utilisateur par son nom d'utilisateur.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired
  UserRepository userRepository;

  /**
   * Charge les détails d'un utilisateur par son nom d'utilisateur.
   * Vérifie également si le compte de l'utilisateur est vérifié avant de renvoyer les détails.
   *
   * @param username Le nom d'utilisateur ou l'email de l'utilisateur.
   * @return Les détails de l'utilisateur pour l'authentification.
   * @throws UsernameNotFoundException Si l'utilisateur n'est pas trouvé ou si le compte n'est pas vérifié.
   */
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Recherche de l'utilisateur par nom d'utilisateur
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + username));

    // Vérifie si l'utilisateur est vérifié
    if (!user.isVerified()) {
      throw new UsernameNotFoundException("Le compte n'est pas encore vérifié.");
    }

    return UserDetailsImpl.build(user);
  }

}