package com.ccms.portal.service;


import com.ccms.portal.dto.request.CardApplicationRequest;
import com.ccms.portal.dto.request.CreateCardRequest;
import com.ccms.portal.dto.response.CardApplicationResponse;
import com.ccms.portal.enums.CardApplicationStatus;
import com.ccms.portal.entity.CardApplicationEntity;
import com.ccms.portal.repository.CardApplicationRepository;
import com.ccms.portal.util.JwtUserDetails;
import com.ccms.portal.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardApplicationService {
    private final JwtUtil jwtUtil;
    private final CardApplicationRepository repository;
    private final CardService cardService;

    public CardApplicationService(CardApplicationRepository repository, JwtUtil jwtUtil, CardService cardService){
        this.jwtUtil = jwtUtil;
        this.repository = repository;
        this.cardService = cardService;
    }

    public CardApplicationResponse apply(CardApplicationRequest application){
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = currentUser.getUserId();
        System.out.println("userId -> " +userId);
        CardApplicationEntity applicationEntity = new CardApplicationEntity();
        applicationEntity.setStatus(CardApplicationStatus.PENDING);
        applicationEntity.setCardTypeId(application.getCardTypeId());
        applicationEntity.setRequestedLimit(application.getRequestLimit());
        applicationEntity.setUserId(userId);
        CardApplicationEntity  cardApplicationEntity= repository.save(applicationEntity);
        return  new CardApplicationResponse(cardApplicationEntity);
    }

    public List<CardApplicationEntity> getApplications(){

        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = currentUser.getUserId();
        return repository.findAllByUserId(userId);
    }
    public Optional<CardApplicationEntity> getApplicationById(Long id){
        return repository.findById(id);
    }

    public CardApplicationResponse updateStatus(Long id, CardApplicationStatus request){
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Long userId = currentUser.getUserId();
        CardApplicationEntity application = repository.findById(id).orElseThrow(()-> new RuntimeException("Application not found"));
        if(!application.getUserId().equals(userId)){
            throw new RuntimeException("You are not authorized to update this application");
        }
        CreateCardRequest createCardRequest = new CreateCardRequest();
        createCardRequest.setCardTypeId(application.getCardTypeId());
        createCardRequest.setUserId(application.getUserId());
        createCardRequest.setCreditLimit(application.getRequestedLimit()*1.0);
        cardService.createCard(createCardRequest);
        application.setReviewDate(LocalDateTime.now());
        application.setReviewerId(userId);
        application.setStatus(request);
        CardApplicationEntity cardApplicationEntity =  repository.save(application);
        CardApplicationResponse cardApplicationResponse = new CardApplicationResponse(cardApplicationEntity);
        return cardApplicationResponse;
    }

    public CardApplicationResponse deleteApplication(Long id) {
        JwtUserDetails currentUser = (JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        Long userId = currentUser.getUserId();
        CardApplicationEntity application  = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card application with id " + id + " not found"));
        CardApplicationResponse cardApplicationResponse = new CardApplicationResponse(application);
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("You are not authorized to delete this application");
        }
        repository.deleteById(id);
        return cardApplicationResponse;
    }
}
