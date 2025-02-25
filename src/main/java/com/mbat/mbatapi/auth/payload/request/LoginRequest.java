package com.mbat.mbatapi.auth.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class LoginRequest {
	@NotBlank
	private String identifier;

	@NotBlank
	private String password;

	private String totp;  // Champ pour le code TOTP


}
