package com.ccms.portal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello! Server is running successfully! 🎉";
  }

  @GetMapping("/status")
  public String status() {
    return "Server is up and running on port 8080";
  }

  @GetMapping("/info")
  public Map<String, Object> info() {
    Map<String, Object> info = new HashMap<>();
    info.put("status", "Server is running");
    info.put("timestamp", LocalDateTime.now());
    info.put("port", 8080);
    info.put("message", "Credit Card Management Portal API is working!");
    return info;
  }
}
