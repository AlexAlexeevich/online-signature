package com.era.onlinesignature.exception;

public class ExceptionConstants {
    public static final String NO_ACCESS = "Доступ запрещен";
    public static final String USER_AND_SUBSCRIBER_AND_CONTRACT_IS_EXIST
            = "Такой инициатор с таким подписантом и таким договором уже существует";
    public static final String USER_IS_EXIST = "Такой пользователь уже существует";
    public static final String USER_NOT_FOUND = "Пользователь не найден";
    public static final String CONTRACT_NOT_FOUND = "Договор не найден";
    public static final String SIGNATURE_NOT_FOUND = "Объект подписания не найден";
    public static final String INVALID_REQUEST_FIELD = "Ошибка ввода";
    public static final String TOO_FREQUENT_REQUEST = "Слишком частый запрос СМС кода";
    public static final String REQUEST_A_NEW_SMS_CODE = "Запросите новый СМС код";
}
