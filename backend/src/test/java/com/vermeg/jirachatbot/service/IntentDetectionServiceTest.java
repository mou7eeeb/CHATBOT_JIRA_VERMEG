package com.vermeg.jirachatbot.service;

import com.vermeg.jirachatbot.model.JiraSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntentDetectionServiceTest {

    private final IntentDetectionService service = new IntentDetectionService();

    @Test
    void genericFrenchTasksDoesNotRestrictIssueType() {
        JiraSearchCriteria criteria =
                service.extractSearchCriteria("affiche moi seulement les tâches qui sont déjà terminées");

        assertEquals("Done", criteria.getStatus());
        assertNull(criteria.getIssueType());
        assertEquals(
                "assignee = currentUser() AND statusCategory = Done",
                service.generateJQLFromCriteria(criteria));
    }

    @Test
    void explicitTaskTypeStillRestrictsIssueType() {
        JiraSearchCriteria criteria =
                service.extractSearchCriteria("affiche les tickets terminés de type Task");

        assertEquals("Task", criteria.getIssueType());
        assertEquals(
                "assignee = currentUser() AND statusCategory = Done AND issuetype = 'Task'",
                service.generateJQLFromCriteria(criteria));
    }
}
