package com.ccms.portal.service;

import com.ccms.portal.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    @Autowired
    CardRepository cardRepository;

    public void getAllCardsByUserId(Long userId){
        System.out.println(cardRepository.findAllByUserIdWithCardType(userId));
    }
}
