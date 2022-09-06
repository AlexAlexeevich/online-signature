package com.era.onlinesignature.repository;

import com.era.onlinesignature.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long>  {
    Optional<Subscriber> findBySubscriberPhoneAndUserId(String subscriberPhone, Long UserId);
    Optional<Subscriber> findBySubscriberPhone(String subscriberPhone);
    Optional<Subscriber> findBySubscriberPhoneAndSmsCode(String subscriberPhone, Long smsCode);
    Optional<Subscriber> findById(Long id);
    Optional<Subscriber> findByIdAndAndSmsCode(Long id, Long smsCode);
}
