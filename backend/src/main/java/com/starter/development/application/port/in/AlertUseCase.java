package com.starter.development.application.port.in;

import com.starter.development.domain.model.Alert;

import java.util.List;

public interface AlertUseCase {

    List<Alert> getAlerts();

    List<Alert> getAlertsByStatus(String status);

    Alert getAlert(Long id);

    Alert updateStatus(Long id, String status, String actor, String reason);
}
