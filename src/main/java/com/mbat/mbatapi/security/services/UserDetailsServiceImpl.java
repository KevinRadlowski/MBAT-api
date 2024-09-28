package com.mbat.mbatapi.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + username));

    // Vérifiez si l'utilisateur est vérifié
    if (!user.isVerified()) {
      throw new UsernameNotFoundException("Le compte n'est pas encore vérifié.");
    }

    return UserDetailsImpl.build(user);
  }

}