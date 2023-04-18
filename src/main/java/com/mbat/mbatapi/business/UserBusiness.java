package com.mbat.mbatapi.business;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mbat.mbatapi.auth.ChangePasswordDto;
import com.mbat.mbatapi.entity.ERole;
import com.mbat.mbatapi.entity.Role;
import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.exception.InvalidEmailException;
import com.mbat.mbatapi.exception.InvalidPasswordException;
import com.mbat.mbatapi.payload.request.LoginRequest;
import com.mbat.mbatapi.payload.request.SignupRequest;
import com.mbat.mbatapi.payload.response.JwtResponse;
import com.mbat.mbatapi.payload.response.MessageResponse;
import com.mbat.mbatapi.repository.RoleRepository;
import com.mbat.mbatapi.repository.UserRepository;
import com.mbat.mbatapi.security.jwt.JwtUtils;
import com.mbat.mbatapi.security.services.UserDetailsImpl;

@Service
public class UserBusiness {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    public void changePassword(ChangePasswordDto passwordDto, User user) throws InvalidPasswordException {
        if (!encoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        user.setPassword(passwordDto.getNewPassword());
        hashPassword(user);

        userRepository.save(user);
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest)
            throws InvalidPasswordException, InvalidEmailException {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Cet utilisateur existe déjà."));
        }
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Erreur: Rôle non trouvé."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Erreur: Rôle non trouvé."));
                        roles.add(adminRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Erreur: Rôle non trouvé."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Utilisateur enregistré avec succès!"));
    }

    private void hashPassword(User user) throws InvalidPasswordException {
        String hashed = encoder.encode(user.getPassword());
        user.setPassword(hashed);
    }

    public ResponseEntity<HttpStatus> deleteUser(Integer id) {
        try {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<User> updateInformationUser(Integer id, User user) throws InvalidParameterException, InvalidEmailException {
        Optional<User> user_update = userRepository.findById(id);
        User __user = user_update.get();
        if (user_update.isPresent()) {
            __user.setUsername(user.getUsername());
            return new ResponseEntity<>(userRepository.save(__user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
