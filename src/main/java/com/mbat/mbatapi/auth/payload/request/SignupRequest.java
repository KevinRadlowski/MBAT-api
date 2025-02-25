package com.mbat.mbatapi.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class SignupRequest {
    @NotBlank
    @Email
    private String username;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Le numéro de téléphone doit contenir exactement 10 chiffres ou être vide.")
    private String phone;

    @NotBlank
    @Size(min = 4, max = 64)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String securityQuestion;

    @NotBlank
    private String securityAnswer;

}