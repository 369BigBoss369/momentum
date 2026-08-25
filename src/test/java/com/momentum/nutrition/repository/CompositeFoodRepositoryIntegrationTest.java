package com.momentum.nutrition.repository;

import com.momentum.core.model.enums.ModerationStatus;
import com.momentum.fitness.model.enums.SourceType;
import com.momentum.nutrition.model.CompositeFood;
import com.momentum.nutrition.model.enums.CompositeFoodType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CompositeFoodRepositoryIntegrationTest {

    @Autowired
    private CompositeFoodRepository compositeFoodRepository;

    private CompositeFood buildCompositeFood(String name, UUID ownerId, SourceType source, boolean isPublic, ModerationStatus status) {
        CompositeFood food = new CompositeFood();
        food.setName(name);
        food.setType(CompositeFoodType.SNACKS);
        food.setCalories(250);
        food.setOwnerId(ownerId);
        food.setSource(source);
        food.setIsPublic(isPublic);
        food.setModerationStatus(status);
        return food;
    }

    @Test
    void save_ShouldPersistCompositeFood() {
        UUID ownerId = UUID.randomUUID();
        CompositeFood food = buildCompositeFood("Trail Mix", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED);

        CompositeFood saved = compositeFoodRepository.save(food);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getType()).isEqualTo(CompositeFoodType.SNACKS);
    }

    @Test
    void findByOwnerIdAndName_ShouldReturnCompositeFood_WhenExists() {
        UUID ownerId = UUID.randomUUID();
        compositeFoodRepository.save(buildCompositeFood("Granola Bar", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<CompositeFood> found = compositeFoodRepository.findByOwnerIdAndName(ownerId, "Granola Bar");

        assertThat(found).isPresent();
    }

    @Test
    void findByIsPublicTrueAndModerationStatus_ShouldReturnOnlyMatching() {
        UUID ownerId = UUID.randomUUID();
        compositeFoodRepository.save(buildCompositeFood("Pending Snack", ownerId, SourceType.CUSTOM, true, ModerationStatus.PENDING));
        compositeFoodRepository.save(buildCompositeFood("Approved Snack", ownerId, SourceType.CUSTOM, true, ModerationStatus.APPROVED));

        List<CompositeFood> pending = compositeFoodRepository.findByIsPublicTrueAndModerationStatus(ModerationStatus.PENDING);

        assertThat(pending).extracting(CompositeFood::getName).containsExactly("Pending Snack");
    }

    @Test
    void findAllAccessible_ShouldIncludeOwnedAndPublicItems() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        compositeFoodRepository.save(buildCompositeFood("My Snack", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));
        compositeFoodRepository.save(buildCompositeFood("Someone Else's Private Snack", otherId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        List<CompositeFood> accessible = compositeFoodRepository.findAllAccessible(ownerId, ownerId);

        assertThat(accessible).extracting(CompositeFood::getName).contains("My Snack");
        assertThat(accessible).extracting(CompositeFood::getName).doesNotContain("Someone Else's Private Snack");
    }

    @Test
    void findCompositeFoodById_ShouldFetchSharedUsersEagerly() {
        UUID ownerId = UUID.randomUUID();
        CompositeFood saved = compositeFoodRepository.save(buildCompositeFood("Energy Bites", ownerId, SourceType.CUSTOM, false, ModerationStatus.APPROVED));

        Optional<CompositeFood> found = compositeFoodRepository.findCompositeFoodById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSharedUsers()).isNotNull();
    }
}
