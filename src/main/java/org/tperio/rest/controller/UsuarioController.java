package org.tperio.rest.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.tperio.DTO.CredenciaisDTO;
import org.tperio.DTO.TokenDTO;
import org.tperio.domain.entity.Usuario;
import org.tperio.exception.SenhaInvalidaException;
import org.tperio.service.JwtUtil;
import org.tperio.service.impl.UsuarioServiceImpl;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    @Autowired
    private final UsuarioServiceImpl usuarioService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtUtil jwtUtil;

    @PostMapping
    @ApiOperation("Cadastra um novo usuario do sistema")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario salvar(@RequestBody  Usuario usuario ){
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        return usuarioService.salvar(usuario);
    }

    @PostMapping("/auth")
    @ApiOperation("Realiza a autenticação do login")
    public TokenDTO autenticar(@RequestBody CredenciaisDTO credenciais){
        try{
            Usuario usuario = Usuario.builder()
                    .login(credenciais.getLogin())
                    .senha(credenciais.getSenha()).build();
            UserDetails usuarioAutenticado = usuarioService.autenticar(usuario);
            String token = jwtUtil.gerarToken(usuario);
            return new TokenDTO(usuario.getLogin(), token);
        } catch (UsernameNotFoundException | SenhaInvalidaException e ){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
