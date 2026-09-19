package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.AlertUseCase;
import com.starter.development.domain.model.Alert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertUseCase alertUseCase;

    public AlertController(AlertUseCase alertUseCase) {
        this.alertUseCase = alertUseCase;
    }

    @GetMapping
    public List<AlertResponse> getAlerts(@RequestParam(required = false) String status) {
        List<Alert> alerts = status == null
                ? alertUseCase.getAlerts()
                : alertUseCase.getAlertsByStatus(status);
        return alerts.stream().map(AlertResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AlertResponse getAlert(@PathVariable Long id) {
        return AlertResponse.from(alertUseCase.getAlert(id));
    }

    @PatchMapping("/{id}/status")
    public AlertResponse updateStatus(@PathVariable Long id,
                                      @RequestBody UpdateAlertStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Alert status request body is required");
        }
        return AlertResponse.from(alertUseCase.updateStatus(id, request.status(),
                request.actor(), request.reason()));
    }
}

record UpdateAlertStatusRequest(String status, String actor, String reason) {
}
