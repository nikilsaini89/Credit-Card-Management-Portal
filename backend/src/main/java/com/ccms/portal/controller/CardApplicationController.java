package com.ccms.portal.controller;

import com.ccms.portal.dto.request.CardApplicationRequest;
import com.ccms.portal.dto.request.UpdateStatusRequest;
import com.ccms.portal.dto.response.CardApplicationResponse;
import com.ccms.portal.entity.CardApplicationEntity;
import com.ccms.portal.service.CardApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("card/application")
public class CardApplicationController {
    @Autowired
    private CardApplicationService cardApplicationService;
    @PostMapping
    public ResponseEntity<?> applyForCard(@Valid @RequestBody CardApplicationRequest application){
        CardApplicationResponse savedApplication = cardApplicationService.apply(application);
        return new ResponseEntity<>(savedApplication,HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<CardApplicationResponse>> getApplications(){
        List<CardApplicationResponse> savedApplications = cardApplicationService.getApplications();
        return new ResponseEntity<>(savedApplications,HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable Long id, @RequestBody UpdateStatusRequest request){
        CardApplicationResponse updatedApplication = cardApplicationService.updateStatus(id,request.getStatus());
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id){
        CardApplicationResponse cardApplicationResponse = cardApplicationService.deleteApplication(id);
        return ResponseEntity.ok(cardApplicationResponse);
    }
}
