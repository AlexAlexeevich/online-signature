package com.era.onlinesignature.service.user;

import com.era.onlinesignature.client.SmsClient;
import com.era.onlinesignature.entity.Contract;
import com.era.onlinesignature.entity.Link;
import com.era.onlinesignature.entity.Role;
import com.era.onlinesignature.entity.Subscriber;
import com.era.onlinesignature.entity.User;
import com.era.onlinesignature.entity.enums.ERole;
import com.era.onlinesignature.exception.BadRequestException;
import com.era.onlinesignature.exception.ExceptionConstants;
import com.era.onlinesignature.model.auth.AuthResponse;
import com.era.onlinesignature.model.contractfile.ContractFileResponse;
import com.era.onlinesignature.repository.ContractRepository;
import com.era.onlinesignature.repository.LinkRepository;
import com.era.onlinesignature.repository.RoleRepository;
import com.era.onlinesignature.repository.SubscriberRepository;
import com.era.onlinesignature.repository.UserRepository;
import com.era.onlinesignature.util.KeyGenerator;
import com.era.onlinesignature.util.Utils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    @Value("${app.jwtSecret}")
    private String tokenSecret;

    @Value("${app.jwtExpirationMs}")
    private Long tokenExpiration;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.host}")
    private String host;

    @Value("${upload.server}")
    private String server;

    @Value("${spring.mail.username}")
    private String email;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriberRepository subscriberRepository;
    private final ContractRepository contractRepository;
    private final LinkRepository linkRepository;
    private final JavaMailSender javaMailSender;
    private final SmsClient smsClient;

    /**
     * Создает инициатора, либо если инициатор уже есть, то добавляет ему нового подписанта или договор.
     *
     * @param file              загружаемые файлы в формате pdf, являются файлами договора
     * @param login             логин (телефон инициатора)
     * @param subscriberPhone   телефон подписанта
     * @param initiatorEmail    электронная почта инициатора
     * @param name              имя инициатора
     * @param nameContract      эксклюзивное название договора
     */
    @Override
    public void enterToService(MultipartFile[] file, String login, String subscriberPhone, String initiatorEmail,
                               String name, String nameContract) {
        Optional<User> currentUser = userRepository.findByLogin(login);
        if (!currentUser.isPresent()) {                            // если юзера нет, то создаем нового юзера
            User newUser = new User();
            newUser.setLogin(login);
            newUser.setInitiatorEmail(initiatorEmail);
            newUser.setName(name);
            insertRole(newUser, "initiator");
            userRepository.save(newUser);
            Optional<User> user = userRepository.findByLogin(login);
            addSubscriber(user.get().getId(), subscriberPhone, file, nameContract);      //добавляем подписанта
            sendSmsCode(login);
        } else {                                            //иначе если юзер есть
            Optional<Subscriber> tempSubscriber = subscriberRepository.findBySubscriberPhoneAndUserId(
                    subscriberPhone, currentUser.get().getId());
            if (tempSubscriber.isPresent()) {
                Optional<Contract> tempContract = contractRepository
                        .findByNameContractAndSubscriberId(nameContract, tempSubscriber.get().getId());
                if (tempContract.isPresent()) {
                    throw new BadRequestException(ExceptionConstants.USER_AND_SUBSCRIBER_AND_CONTRACT_IS_EXIST);
                }
            }
            //checkTimeOfLastSendingOfCodeAndSendSmsCode(user.get());
            User user = currentUser.get();
            Date dateSmsCode = user.getDateCodeEnter();
            Date dateNow = new Date();
            if (dateSmsCode != null) {                       //проверяем, была ли уже отправка кода юзеру
                if (Utils.getDateDifference(dateSmsCode, dateNow, TimeUnit.SECONDS) < 60) {  //прошло ли 60 сек с момента последней отправки кода
                    throw new BadRequestException(ExceptionConstants.TOO_FREQUENT_REQUEST);
                } else {
                    addSubscriber(user.getId(), subscriberPhone, file, nameContract);
                }
            } else {
                addSubscriber(user.getId(), subscriberPhone, file, nameContract);
            }
        }
    }

    private void addSubscriber(Long userId, String subscriberPhone, MultipartFile[] file, String nameContract) {
        Optional<Subscriber> currentSubscriber = subscriberRepository.findBySubscriberPhoneAndUserId(subscriberPhone, userId);
        if (!currentSubscriber.isPresent()) {
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setUserId(userId);
            newSubscriber.setSubscriberPhone(subscriberPhone);
            subscriberRepository.save(newSubscriber);
            Optional<Subscriber> subscriber = subscriberRepository.findBySubscriberPhoneAndUserId(subscriberPhone, userId);
            addContract(subscriber.get().getId(), file, nameContract);
        } else {
            addContract(currentSubscriber.get().getId(), file, nameContract);
        }
    }

    private void addContract(Long subscriberId, MultipartFile[] file, String nameContract) {
        Optional<Contract> currentContract = contractRepository
                .findByNameContractAndSubscriberId(nameContract, subscriberId);
        if (!currentContract.isPresent()) { //мы его всяко нашли, и добавляем договор
            Contract newContract = new Contract();
            String shortUrl = createShortUrl();
            newContract.setShortUrl(shortUrl);
            newContract.setDateCreateShortUrl(new Date());
            newContract.setSubscriberId(subscriberId);
            newContract.setNameContract(nameContract);
            contractRepository.save(newContract);

            Optional<Contract> contract = contractRepository.findByNameContractAndSubscriberId(nameContract, subscriberId);
            if (contract.isPresent()) {
                for (int i = 0; i < file.length; i++) {
                    File uploadDir = new File(uploadPath);
                    if(!uploadDir.exists()) {
                        uploadDir.mkdir();
                    }
                    String uuidFile = UUID.randomUUID().toString();
                    String resultFileName = uuidFile + "." + file[i].getOriginalFilename();
                    try {
                        file[i].transferTo(new File(uploadPath + resultFileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Link newLink = new Link();
                    newLink.setContractId(contract.get().getId());
                    newLink.setNameLinkContract(file[i].getOriginalFilename());
                    newLink.setLinkContract(uploadPath + resultFileName);
                    newLink.setFileExtension("pdf");
                    linkRepository.save(newLink);
                    //listNameLinkContract.add(file[i].getOriginalFilename());
                    //String fileName = file[i].getOriginalFilename();
                    //int position = fileName.lastIndexOf(".");
                    //fileName = position > 0 ? fileName.substring(0, position) : fileName;
                    //contract.setContentType(file[i].getContentType());
                }
            }
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Если инициатор аутентифицирован, отправляет подписанту короткую ссылку на телефон
     *
     * @param login             логин (телефон инициатора)
     * @param subscriberPhone   телефон подписанта
     * @param nameContract      эксклюзивное название договора
     */
    @Override
    public void sendShortUrl(String login, String subscriberPhone, String nameContract) {
        System.out.println("Логин инициатора: " + login);
        Optional<User> currentUser = userRepository.findByLogin(login);
        if (currentUser.isPresent()) { //если юзер есть, то отправляем ему ссылку
            User newUser = currentUser.get();
            Optional<Subscriber> subscriber = subscriberRepository.findBySubscriberPhoneAndUserId(subscriberPhone,
                    newUser.getId());
            Long id = null;
            if (subscriber.isPresent()) {
                id = subscriber.get().getId();
            }
            String shortUrl = null;
            Optional<Contract> contract = contractRepository.findByNameContractAndSubscriberId(nameContract, id);
            if (contract.isPresent()) {
                shortUrl = contract.get().getShortUrl();
            }
            System.out.println("Подписант и короткая ссылка для него: " + subscriberPhone + " " + shortUrl);
            //smsClient.sendLink(subscriberPhone, host + shortUrl);
        } else { //иначе если юзера нет
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Если инициатор аутентифицирован, отправляет инициатору смс код для верификации
     *
     * @param login          логин (телефон инициатора)
     */
    public void sendSmsCode(String login) {
        Optional<User> user = userRepository.findByLogin(login);
        if (user.isPresent()) {
            User currentUser = user.get();
            currentUser.setCountSendSmsCode(currentUser.getCountSendSmsCode() + 1);
            Date dateSmsCode = currentUser.getDateCodeEnter();
            Date dateNow = new Date();
            int countSendSmsCode = currentUser.getCountSendSmsCode();
            if (countSendSmsCode > 3) {
                if (Utils.getDateDifference(dateSmsCode, dateNow, TimeUnit.SECONDS) < 180) {//180
                    throw new BadRequestException(ExceptionConstants.REQUEST_A_NEW_SMS_CODE);
                } else {
                    currentUser.setCountSendSmsCode(0);
                }
            }
            String code = generateCode();
            System.out.println("смс код: " + code);
            currentUser.setSmsCode(Long.valueOf(code));
            currentUser.setDateCodeEnter(new Date());
            userRepository.save(currentUser);
            //smsClient.sendSms(login, code);
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Верифицирует инициатора, и для верифицированного инициатора создает токен.
     * После этого отправляет подписанту короткую ссылку на его телефон.
     *
     * @param login             логин (телефон инициатора)
     * @param code              смс код для верификации
     * @param subscriberPhone   телефон подписанта
     * @param nameContract      эксклюзивное название договора
     *
     * @return возвращает объектов AuthResponse, который включает в себя данные о токене
     */
    @Override
    public AuthResponse sendCodeFromInitiator(String login, String code, String subscriberPhone, String nameContract) {
        System.out.println("Логин и код инициатора: " + login + " " + code);
        Optional<User> currentUser = userRepository.findByLoginAndSmsCode(login, Long.valueOf(code));
        if (currentUser.isPresent()) { //если юзер есть, то создаем токен
            User newUser = currentUser.get();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + tokenExpiration);
            String authToken = Jwts.builder()
                    .setSubject(Long.toString(newUser.getId()))
                    .setIssuedAt(new Date())
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, tokenSecret)
                    .compact();
            newUser.setSmsCode(null);
            newUser.setDateCodeEnter(null);
            newUser.setCountSendSmsCode(0);
            userRepository.save(newUser);
            System.out.println("Token: " + authToken);

            //отправка линка подписанту
            Optional<Subscriber> subscriber = subscriberRepository.findBySubscriberPhoneAndUserId(subscriberPhone,
                    newUser.getId());
            Long id = null;
            if (subscriber.isPresent()) {
                id = subscriber.get().getId();
            }
            String shortUrl = null;
            Optional<Contract> contract = contractRepository.findByNameContractAndSubscriberId(nameContract, id);
            if (contract.isPresent()) {
                shortUrl = contract.get().getShortUrl();
            }
            System.out.println("Подписант и короткая ссылка для него: " + subscriberPhone + " " + host + shortUrl);
            //smsClient.sendLink(subscriberPhone, host + shortUrl);
            return new AuthResponse(authToken, newUser.getRoles());
        } else { //иначе если код ошибочен, отсчитываем кол-во ошибок ввода
            Optional<User> user2 = userRepository.findByLogin(login);
            if (user2.isPresent()) {
                User currentUser2 = user2.get();
                currentUser2.setCountSendSmsCode(currentUser2.getCountSendSmsCode() + 1);
                int countInputAttempt = currentUser2.getCountSendSmsCode();
                userRepository.save(currentUser2);
                if (countInputAttempt > 3) {
                    currentUser2.setCountSendSmsCode(0);
                    currentUser2.setDateCodeEnter(null);
                    currentUser2.setSmsCode(null);
                    userRepository.save(currentUser2);
                    throw new BadRequestException(ExceptionConstants.REQUEST_A_NEW_SMS_CODE);
                }
            }
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Отправляет смс код подписанту.
     * После этого отправляет подписанту короткую ссылку на его телефон.
     *
     * @param shortUrl      короткая ссылка
     */
    @Override
    @Transactional
    public void getSmsCodeForSubscriber(String shortUrl) {
        Optional<Contract> contract = contractRepository.findByShortUrl(shortUrl);
        if (contract.isPresent()) {
            Optional<Subscriber> subscriber = subscriberRepository.findById(contract.get().getSubscriberId());
            if (subscriber.isPresent()) {
                Subscriber currentSubscriber = subscriber.get();
                Date dateSmsCode = currentSubscriber.getDateCodeEnter();
                Date dateNow = new Date();
                if (dateSmsCode != null) {                       //проверяем, была ли уже отправка кода подписанту
                    if (Utils.getDateDifference(dateSmsCode, dateNow, TimeUnit.SECONDS) < 60) {  //прошло ли 60 сек с момента последней отправки кода
                        throw new BadRequestException(ExceptionConstants.TOO_FREQUENT_REQUEST);
                    } else {
                        sendSmsCodeToSubscriber(currentSubscriber);
                    }
                } else {
                    sendSmsCodeToSubscriber(currentSubscriber);
                }
            } else {
                throw new BadRequestException(ExceptionConstants.NO_ACCESS);
            }
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    private void sendSmsCodeToSubscriber(Subscriber subscriber) {
        subscriber.setCountSendSmsCode(subscriber.getCountSendSmsCode() + 1);
        Date dateSmsCode = subscriber.getDateCodeEnter();
        Date dateNow = new Date();
        int countSendSmsCode = subscriber.getCountSendSmsCode();

        if (countSendSmsCode > 3) {
            if (Utils.getDateDifference(dateSmsCode, dateNow, TimeUnit.SECONDS) < 180) {//180
                throw new BadRequestException(ExceptionConstants.REQUEST_A_NEW_SMS_CODE);
            } else {
                subscriber.setCountSendSmsCode(0);
            }
        }
        String code = generateCode();
        System.out.println(code);
        subscriber.setSmsCode(Long.valueOf(code));
        subscriber.setDateCodeEnter(new Date());
        subscriberRepository.save(subscriber);
        //smsClient.sendSms(subscriber.getSubscriberPhone(), code);
    }

    /**
     * Верифицирует подписанта.
     * После этого, отправляет подписанту короткую ссылку на его телефон.
     *
     * @param shortUrl      короткая ссылка
     * @param code          смс код
     *
     * @return возвращает значение список объектов Link (файл договора)
     */
    @Override
    public List<ContractFileResponse> sendCodeFromSubscriber(String shortUrl, String code) {
        Optional<Contract> contract = contractRepository.findByShortUrl(shortUrl);
        Long subscriberId;
        if (contract.isPresent()) {
            subscriberId = contract.get().getSubscriberId();
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
        Optional<Subscriber> subscriber = subscriberRepository.findByIdAndAndSmsCode(subscriberId, Long.valueOf(code));
        if (subscriber.isPresent()) {
            Subscriber currentSubscriber = subscriber.get();
            currentSubscriber.setSmsCode(null);
            currentSubscriber.setDateCodeEnter(null);
            currentSubscriber.setCountSendSmsCode(0);
            subscriberRepository.save(currentSubscriber);
            List<Link> listLinks = linkRepository.findAllByContractId(contract.get().getId());
            List<ContractFileResponse> files = new ArrayList<>();
            for (int i = 0; i < listLinks.size(); i++) {
                files.add(new ContractFileResponse(listLinks.get(i).getId(), listLinks.get(i).getNameLinkContract(),
                        server + "api/subscriber/open_contract/" + listLinks.get(i).getId(), listLinks.get(i).getFileExtension()));
            }
            return files;
        } else {
            subscriber = subscriberRepository.findById(subscriberId);
            if (subscriber.isPresent()) {
                Subscriber currentSubscriber2  = subscriber.get();
                currentSubscriber2.setCountSendSmsCode(currentSubscriber2.getCountSendSmsCode() + 1);
                int countInputAttempt = currentSubscriber2.getCountSendSmsCode();
                subscriberRepository.save(currentSubscriber2);
                if (countInputAttempt > 3) {
                    currentSubscriber2.setCountSendSmsCode(0);
                    currentSubscriber2.setDateCodeEnter(null);
                    currentSubscriber2.setSmsCode(null);
                    subscriberRepository.save(currentSubscriber2);
                    throw new BadRequestException(ExceptionConstants.REQUEST_A_NEW_SMS_CODE);
                }
            }
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Отправляет смс код подписанту для подписания договора.
     *
     * @param shortUrl           короткая ссылка
     * @param subscriberEmail    телефон подписанта
     */
    @Override
    @Transactional
    public void getCodeForSigning(String shortUrl, String subscriberEmail) {
        Optional<Contract> contract = contractRepository.findByShortUrl(shortUrl);
        if (contract.isPresent()) {
            Optional<Subscriber> subscriber = subscriberRepository.findById(contract.get().getSubscriberId());
            if (subscriber.isPresent()) {
                Subscriber currentSubscriber = subscriber.get();
                currentSubscriber.setSubscriberEmail(subscriberEmail);
                Date dateSmsCode = currentSubscriber.getDateCodeEnter();
                Date dateNow = new Date();
                if (dateSmsCode != null) {                       //проверяем, была ли уже отправка кода подписанту
                    if (Utils.getDateDifference(dateSmsCode, dateNow, TimeUnit.SECONDS) < 3600) {  //прошло ли 60 мин с момента последней отправки кода
                        throw new BadRequestException(ExceptionConstants.TOO_FREQUENT_REQUEST);
                    } else {
                        sendSmsCodeToSubscriber(currentSubscriber);
                    }
                } else {
                    sendSmsCodeToSubscriber(currentSubscriber);
                }
            }
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    /**
     * Подписывает договор, и отправляет копии договора на электронные адреса инициатора и подписанта.
     *
     * @param code               смс код
     * @param shortUrl           короткая ссылка
     */
    @Override
    public void signContract(String code, String shortUrl) {
        Optional<Contract> contract = contractRepository.findByShortUrl(shortUrl);
        if (contract.isPresent()) {
            Optional<Subscriber> subscriber = subscriberRepository.findById(contract.get().getSubscriberId());
            if (subscriber.isPresent()) {
                Optional<User> user = userRepository.findById(subscriber.get().getUserId());
                if (user.isPresent()) {
                    //отправка договора инициатору и подписанту
                    System.out.println("Почты для отправки: " + user.get().getInitiatorEmail() + "  " + subscriber.get().getSubscriberEmail());
                    MimeMessage message = javaMailSender.createMimeMessage();
                    try {
                        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,
                                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                                StandardCharsets.UTF_8.name());

                        String[] mailSendAddressArray = {user.get().getInitiatorEmail(), subscriber.get().getSubscriberEmail()};
                        mimeMessageHelper.setTo(mailSendAddressArray);
                        mimeMessageHelper.setSubject("Подписанный договор");
                        mimeMessageHelper.setFrom(email);
                        mimeMessageHelper.setText("Подписанный договор", true);
                        List<Link> link = linkRepository.findAllByContractId(contract.get().getId());
                        //int quantityFile = currentUser.getContracts().size();
                        for (int i = 0; i < link.size(); i++) {
                            System.out.println(link.get(i).getLinkContract());
                            mimeMessageHelper.addAttachment(link.get(i).getLinkContract(),
                                    new FileSystemResource(new File(link.get(i).getLinkContract())));
                        }
                        //javaMailSender.send(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new BadRequestException(ExceptionConstants.NO_ACCESS);
                }
            } else {
                throw new BadRequestException(ExceptionConstants.NO_ACCESS);
            }
        } else {
            throw new BadRequestException(ExceptionConstants.NO_ACCESS);
        }
    }

    private String generateCode() {
        KeyGenerator keyGenerator = new KeyGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(false)
                .useUpper(false)
                .usePunctuation(false)
                .build();
        return keyGenerator.generate(6);
    }

    private String createShortUrl() {
        String randomString = "";
        String possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 5; i++) {
            randomString += possibleChars.charAt((int) Math.floor(Math.random() * possibleChars.length()));
        }
        String shortUrl = randomString;
//        String shortUrl = host + randomString;
        return shortUrl;
    }

    /**
     * Ищет в базе данных объект типа User по его id.
     *
     * @param id      id инициатора
     *
     * @return возвращает объект типа Optional, содержащий либо найденный объект User из базы данных,
     * либо null, если объект не найден в базе данных.
     */
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Ищет в базе данных объект типа User по его логину.
     *
     * @param login      логин (телефон инициатора)
     *
     * @return возвращает объект типа Optional, содержащий либо найденный объект User из базы данных,
     * либо null, если объект не найден в базе данных.
     */
    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    private void insertRole(User user, String role) {
        Set<Role> roles = new HashSet<>();
        switch (role) {
            case "admin":
                Role adminRole = roleRepository
                        .findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error, Role ADMIN is not found"));
                roles.add(adminRole);
                break;

            case "initiator":
                Role initRole = roleRepository
                        .findByName(ERole.ROLE_INITIATOR)
                        .orElseThrow(() -> new RuntimeException("Error, Role INITIATOR is not found"));
                roles.add(initRole);
                break;
        }
        user.setRoles(roles);
    }

    private void test() {}
    HashMap<Integer, Integer> hashMap = new HashMap();
}