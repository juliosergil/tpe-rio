package org.tperio.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tperio.DTO.HelloDTO;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping
    public HelloDTO userHello(){
        return HelloDTO.builder().nome("Júlio Lima - Usuário").msg("Bem Vindo ao TPE Rio de Janeiro 2022!").build();
    }
}
