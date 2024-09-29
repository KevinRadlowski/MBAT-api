package com.mbat.mbatapi.security.services;

import com.mbat.mbatapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.repository.UserRepository;

import java.util.Date;

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
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

    if (user.isAccountLocked()) {
      long lockTimeRemaining = (user.getLockTime().getTime() + UserService.LOCK_TIME_DURATION - System.currentTimeMillis()) / 1000;
      throw new LockedException("Le compte est verrouillé. Temps restant avant déblocage : " + lockTimeRemaining + " secondes.");
    }

    if (!user.isVerified()) {
      throw new UsernameNotFoundException("Le compte n'est pas encore vérifié.");
    }

    return UserDetailsImpl.build(user);
  }
}