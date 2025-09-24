package com.ccms.portal.controller;

import com.ccms.portal.dto.request.CardApplicationRequest;
import com.ccms.portal.dto.request.UpdateStatusRequest;
import com.ccms.portal.model.CardApplicationEntity;
import com.ccms.portal.service.CardApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("card/application")
public class CardApplicationController {
    @Autowired
    private CardApplicationService cardApplicationService;
    @PostMapping
    public ResponseEntity<CardApplicationEntity> applyForCard(@Valid @RequestBody CardApplicationRequest application){
        CardApplicationEntity savedApplication = cardApplicationService.apply(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApplication);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<CardApplicationEntity>> getApplications(@PathVariable Long userId){
        List<CardApplicationEntity> savedApplications = cardApplicationService.getApplications(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApplications);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardApplicationEntity> updateApplication(@PathVariable Long id, @RequestBody UpdateStatusRequest request){
        CardApplicationEntity updatedApplication = cardApplicationService.updateStatus(id,request.getStatus());
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id){
        Optional<CardApplicationEntity> deletedApplication = cardApplicationService.getApplicationById(id);
        if(deletedApplication.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card with id"+id+" not found");
        }
        cardApplicationService.deleteApplication(id);
        return ResponseEntity.ok("Card application deleted successfully");
    }
}
