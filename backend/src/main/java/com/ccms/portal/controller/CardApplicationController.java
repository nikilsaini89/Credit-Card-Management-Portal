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
        return new ResponseEntity<>(savedApplication,HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CardApplicationEntity>> getApplications(){
        List<CardApplicationEntity> savedApplications = cardApplicationService.getApplications();
        return new ResponseEntity<>(savedApplications,HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardApplicationEntity> updateApplication(@PathVariable Long id, @RequestBody UpdateStatusRequest request){
        CardApplicationEntity updatedApplication = cardApplicationService.updateStatus(id,request.getStatus());
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id){
        cardApplicationService.deleteApplication(id);
        return ResponseEntity.ok("Card application deleted successfully");
    }
}
