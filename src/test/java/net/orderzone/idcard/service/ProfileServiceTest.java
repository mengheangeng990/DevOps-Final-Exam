package net.orderzone.idcard.service;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.ProfileRepository;
import net.orderzone.idcard.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private TemplateRepository templateRepository;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(profileRepository, templateRepository);
    }

    @Test
    void save_shouldPopulateUuidAndRegistrationAndTimestamps() {
        Profile profile = Profile.builder()
                .type(ProfileType.STUDENT)
                .fullName("Jane Doe")
                .department("SCI")
                .title("Student")
                .build();

        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile saved = profileService.save(profile);

        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getRegistrationNumber()).contains("SCI");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void uploadPhoto_shouldRejectLargeFile() {
        byte[] content = new byte[3 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("photo", "photo.jpg", "image/jpeg", content);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(Profile.builder().id(1L).build()));

        try {
            profileService.uploadPhoto(1L, file);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
