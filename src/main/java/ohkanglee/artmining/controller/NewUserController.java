package ohkanglee.artmining.controller;

import ohkanglee.artmining.domain.Account;
import ohkanglee.artmining.domain.Token;
import ohkanglee.artmining.repository.AccountRepository;
import ohkanglee.artmining.jwt.JwtTokenUtil;
import ohkanglee.artmining.service.JwtUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping
public class NewUserController {
    private Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private AccountRepository accountRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager am;


}