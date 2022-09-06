package com.era.onlinesignature.controller;

import com.era.onlinesignature.entity.Link;
import com.era.onlinesignature.exception.BadRequestException;
import com.era.onlinesignature.exception.ExceptionConstants;
import com.era.onlinesignature.model.contractfile.ContractFileResponse;
import com.era.onlinesignature.repository.LinkRepository;
import com.era.onlinesignature.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriber")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags={"Контроллер подписанта"})
public class SubscriberController {

    private final UserService userService;
    private final LinkRepository linkRepository;

    @ApiOperation(value = "Получение sms для верификации подписанта",
            authorizations = @Authorization(value = "Bearer"))
    @GetMapping("/get_sms")
    public ResponseEntity<HttpStatus> getSms(@RequestParam("short_url") String shortUrl) {
        System.out.println("Получение sms для верификации подписанта " + shortUrl);
        userService.getSmsCodeForSubscriber(shortUrl);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Отправка кода для верификации подписанта", authorizations = @Authorization(value = "Bearer"))
    @PostMapping("/send_code")
    public ResponseEntity<List<ContractFileResponse>> sendCodeFromSubscriber(@RequestParam("short_url") String shortUrl,
                                                                             @RequestParam("code") String code) {
        List<ContractFileResponse> files = userService.sendCodeFromSubscriber(shortUrl, code);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @ApiOperation(value = "Открытие договора подписантом из приложения. " +
            "Swagger не отображает внутри себя pdf. Воспользуйтесь другим инструментом тестирования API.",
            authorizations = @Authorization(value = "Bearer"))
    @GetMapping("/open_contract/{id}")
    public void openContract(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
        Optional<Link> link = linkRepository.findById(Long.valueOf(id));
        if (link.isPresent()) {
            String pdfFilename =  link.get().getLinkContract();
                    File file = new File(pdfFilename);
        byte[] pdfBytes = Files.readAllBytes(file.toPath());
        try {
            InputStream is = new ByteArrayInputStream(pdfBytes);
            IOUtils.copy(is, response.getOutputStream());
            response.addHeader("Content-type", "application/pdf");
            response.flushBuffer();

        } catch (NullPointerException | IOException ex) {
            throw new NullPointerException();
        }

        } else {
            throw new BadRequestException(ExceptionConstants.CONTRACT_NOT_FOUND);
        }
   }

    @ApiOperation(value = "Получение sms для подписания договора", authorizations = @Authorization(value = "Bearer"))
    @GetMapping("/get_sms_for_signing")
    public ResponseEntity<HttpStatus> getSmsForSigning(@RequestParam("short_url") String shortUrl,
                                             @RequestParam("subscriber_email") String subscriberEmail) {
        userService.getCodeForSigning(shortUrl, subscriberEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Подписание договора подписантом через sms", authorizations = @Authorization(value = "Bearer"))
    @PostMapping("/sign_contract")
    public ResponseEntity<HttpStatus> signContract(@RequestParam("code") String code,
                                                   @RequestParam("short_url") String shortUrl) {
        userService.signContract(code, shortUrl);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
