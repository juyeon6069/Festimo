package com.example.festimo.domain.oauth.controller;

import com.example.festimo.domain.user.repository.UserRepository;
import com.example.festimo.domain.user.service.UserService;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/oauth2/users")
@RequiredArgsConstructor
public class OAuth2Controller {

	private final UserRepository userRepository;
	private final UserService userService;


}
