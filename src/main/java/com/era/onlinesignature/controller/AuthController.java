package com.era.onlinesignature.controller;

import com.era.onlinesignature.model.auth.AuthResponse;
import com.era.onlinesignature.repository.UserRepository;
import com.era.onlinesignature.security.TokenProvider;
import com.era.onlinesignature.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags={"Контроллер авторизации"})
public class AuthController {

    private final UserService userService;

    @ApiOperation(value = "Авторизация. Swagger не корректно работает при вводе нескольких файлов." +
            " Воспользуйтесь другим инструментом тестирования API.", authorizations = @Authorization(value = "Bearer"))
    @ApiResponses({
            @ApiResponse(code = 200, message = "Успешно"),
            @ApiResponse(code = 500, message = "Ошибка сервера"),
    })
    @PostMapping(value = "/signin",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<HttpStatus> authUser(@RequestParam(value = "file") MultipartFile[] file,
                                               @RequestParam(value = "login") String login,
                                               @RequestParam(value = "subscriber_phone") String subscriberPhone,
                                               @RequestParam(value = "initiator_email") String initiatorEmail,
                                               @RequestParam(value = "name", required = false) String name,
                                               @RequestParam(value = "name_contract") String nameContract) {
        userService.enterToService(file, login, subscriberPhone, initiatorEmail, name, nameContract);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Отправка смс-кода не аутентифицированному инициатору", authorizations = @Authorization(value = "Bearer"))
    @ApiResponses({
            @ApiResponse(code = 200, message = "Успешно"),
            @ApiResponse(code = 500, message = "Ошибка сервера"),
    })
    @PostMapping("/sendsms")
    public ResponseEntity<AuthResponse> sendCodeFromInitiator(@RequestParam("login") String login,
                                                              @RequestParam("code") String code,
                                                              @RequestParam("name_contract") String nameContract,
                                                              @RequestParam("subscriber_phone") String subscriberPhone) {
        return new ResponseEntity<>(userService.sendCodeFromInitiator(login, code, subscriberPhone, nameContract), HttpStatus.OK);
    }

//    @ApiOperation(value = "Получение смс кода для аутентифицированного инициатора", authorizations = @Authorization(value = "Bearer"))
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "Успешно"),
//            @ApiResponse(code = 500, message = "Ошибка сервера"),
//    })
//    @PostMapping("/get_code")
//    public ResponseEntity<HttpStatus> getCodeForInitiator(@RequestParam("login") String login) {
//        userService.sendSmsCode(login);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    @ApiOperation(value = "Отправка короткой ссылки подписанту при аутентифицированном инициаторе", authorizations = @Authorization(value = "Bearer"))
    @ApiResponses({
            @ApiResponse(code = 200, message = "Успешно"),
            @ApiResponse(code = 500, message = "Ошибка сервера"),
    })
    @PostMapping(value = "/send_short_url", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void sendShortUrl(@RequestParam("login") String login, @RequestParam("name_contract") String nameContract,
                             @RequestParam("subscriber_phone") String subscriberPhone) {
        userService.sendShortUrl(login, subscriberPhone, nameContract);
    }
}
