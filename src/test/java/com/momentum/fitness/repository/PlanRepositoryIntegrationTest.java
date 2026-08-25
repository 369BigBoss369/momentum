package com.momentum.fitness.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.Plan;
import com.momentum.fitness.model.enums.PlanType;
import com.momentum.fitness.model.enums.SourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PlanRepositoryIntegrationTest {

    @Autowired
    private PlanRepository planRepository;

    private Plan buildPlan(String name, UUID ownerId, SourceType source, boolean isPublic, ModerationStatus status) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setDescription("A test plan");
        plan.setType(PlanType.STRENGTH);
        plan.setOwnerId(ownerId);
        plan.setSource(source);
        plan.setIsPublic(isPublic);
        plan.setModerationStatus(status);
        return plan;
    }

    @Test
    void save_ShouldPersistPlan() {
        UUID ownerId = UUID.randomUUID();
        Plan plan = buildPlan("12-Week Strength Program", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED);

        Plan saved = planRepository.save(plan);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(PlanType.STRENGTH);
        assertThat(saved.getPlanDays()).isEmpty();
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnPlan_WhenExists() {
        UUID ownerId = UUID.randomUUID();
        planRepository.save(buildPlan("Summer Shred", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Plan> found = planRepository.findByOwnerIdAndName(ownerId, "Summer Shred");

        assertThat(found).isPresent();
    }

    @Test
    void findAllByOwnerId_ShouldReturnOnlyThatOwnersPlans() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        planRepository.save(buildPlan("My Plan", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));
        planRepository.save(buildPlan("Their Plan", otherId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        List<Plan> ownedPlans = planRepository.findAllByOwnerId(ownerId);

        assertThat(ownedPlans).extracting(Plan::getName).containsExactly("My Plan");
    }

    @Test
    void findByIsPublicTrueAndModerationStatus_ShouldReturnOnlyMatching() {
        UUID ownerId = UUID.randomUUID();
        planRepository.save(buildPlan("Pending Plan", ownerId, SourceType.CUSTOM, true, ModerationStatus.PENDING));
        planRepository.save(buildPlan("Approved Plan", ownerId, SourceType.CUSTOM, true, ModerationStatus.APPROVED));

        List<Plan> pending = planRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);

        assertThat(pending).extracting(Plan::getName).containsExactly("Pending Plan");
    }

    @Test
    void search_ShouldReturnMatchingPlansByQuery() {
        UUID ownerId = UUID.randomUUID();
        planRepository.save(buildPlan("Beginner Strength Plan", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Page<Plan> results = planRepository.search("Beginner", null, ownerId, PageRequest.of(0, 10));

        assertThat(results.getContent()).extracting(Plan::getName).contains("Beginner Strength Plan");
    }

    @Test
    void findByIdWithSharedUsers_ShouldFetchSharedUsersEagerly() {
        UUID ownerId = UUID.randomUUID();
        Plan saved = planRepository.save(buildPlan("Endurance Builder", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<Plan> found = planRepository.findByIdWithSharedUsers(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSharedUsers()).isNotNull();
    }
}
