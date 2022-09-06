package com.era.onlinesignature.service.user;

import com.era.onlinesignature.entity.Link;
import com.era.onlinesignature.entity.User;
import com.era.onlinesignature.model.auth.AuthResponse;
import com.era.onlinesignature.model.contractfile.ContractFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void enterToService(MultipartFile[] file, String login, String subscriberPhone, String initiatorEmail, String name,
                        String nameContract);
    void sendShortUrl(String login, String subscriberPhone, String nameContract);
    void getSmsCodeForSubscriber(String shortUrl);
    void getCodeForSigning(String shortUrl, String subscriberEmail);
    void signContract(String code, String shortUrl);
    void sendSmsCode(String login);
    AuthResponse sendCodeFromInitiator(String login, String code,  String subscriberPhone, String nameContract);
    Optional<User> findById(Long id);
    Optional<User> findByLogin(String login);
    List<ContractFileResponse> sendCodeFromSubscriber(String subscriberPhone, String code);
}
