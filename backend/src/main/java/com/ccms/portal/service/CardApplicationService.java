package com.ccms.portal.service;


import com.ccms.portal.dto.request.CardApplicationRequest;
import com.ccms.portal.enums.CardApplicationStatus;
import com.ccms.portal.model.CardApplicationEntity;
import com.ccms.portal.repository.CardApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardApplicationService {
    private final CardApplicationRepository repository;

    public CardApplicationService(CardApplicationRepository repository){
        this.repository = repository;
    }

    public CardApplicationEntity apply(CardApplicationRequest application){
        CardApplicationEntity applicationEntity = new CardApplicationEntity();
        applicationEntity.setStatus(CardApplicationStatus.Pending);
        applicationEntity.setCardTypeId(application.getCardTypeId());
        applicationEntity.setUserId(application.getUserId());
        return repository.save(applicationEntity);
    }

    public List<CardApplicationEntity> getApplications(Long userId){
        return repository.findAllByUserId(userId);
    }
    public Optional<CardApplicationEntity> getApplicationById(Long id){
        return repository.findById(id);
    }

    public CardApplicationEntity updateStatus(Long id, CardApplicationStatus request){
        CardApplicationEntity application = repository.findById(id).orElseThrow(()-> new RuntimeException("Application not found"));
        application.setStatus(request);
        return repository.save(application);
    }

    public void deleteApplication(Long id) {
        repository.deleteById(id);
    }
}
