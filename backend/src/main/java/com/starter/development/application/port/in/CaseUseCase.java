package com.starter.development.application.port.in;

import com.starter.development.domain.model.Case;

import java.util.List;

public interface CaseUseCase {

    Case createCase(Long alertId, String title, String description, Long analystId);

    List<Case> getCases();

    Case getCase(Long id);

    Case assignCase(Long id, Long analystId);

    Case updateDisposition(Long id, String status, String disposition, String notes, String actor);
}
