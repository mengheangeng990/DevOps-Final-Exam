package net.orderzone.idcard.service;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.ProfileType;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.repository.ProfileRepository;
import net.orderzone.idcard.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final TemplateRepository templateRepository;
    private final Path photoDir;

    public ProfileService(ProfileRepository profileRepository,
                          TemplateRepository templateRepository) {
        this.profileRepository = profileRepository;
        this.templateRepository = templateRepository;
        this.photoDir = Paths.get("photos").toAbsolutePath();
        try {
            Files.createDirectories(photoDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create photo storage directory", e);
        }
    }

    public List<Profile> listAll() {
        return profileRepository.findAll();
    }

    public Optional<Profile> findById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> findByUuid(String uuid) {
        return profileRepository.findByUuid(uuid);
    }

    public Profile save(Profile profile) {
        if (profile.getUuid() == null) {
            profile.setUuid(UUID.randomUUID().toString());
        }
        if (profile.getRegistrationNumber() == null) {
            profile.setRegistrationNumber(generateCustomRegistration(profile));
        }
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        if (profile.getIssueDate() == null) {
            profile.setIssueDate(LocalDate.now());
        }
        if (profile.getExpiryDate() == null) {
            profile.setExpiryDate(profile.getIssueDate().plusYears(2));
        }
        return profileRepository.save(profile);
    }

    public void delete(Long id) {
        profileRepository.deleteById(id);
    }

    public List<Profile> searchByName(String query) {
        return profileRepository.findByFullNameContainingIgnoreCase(query);
    }

    public Profile uploadPhoto(Long profileId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Photo file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Only JPEG and PNG files are allowed");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Photo size must be under 2MB");
        }
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileName = "profile-" + profileId + extension;
        Path destination = photoDir.resolve(fileName);
        Files.copy(file.getInputStream(), destination);
        profile.setPhotoFileName(fileName);
        profile.setPhotoContentType(contentType);
        return profileRepository.save(profile);
    }

    public byte[] getPhotoBytes(Profile profile) throws IOException {
        if (!profile.hasPhoto()) {
            return new byte[0];
        }
        return Files.readAllBytes(photoDir.resolve(profile.getPhotoFileName()));
    }

    public String generateCustomRegistration(Profile profile) {
        String prefix = profile.getType().name().substring(0, 3);
        String dept = profile.getDepartment() != null ? profile.getDepartment().toUpperCase().replaceAll("[^A-Z0-9]", "") : "GEN";
        String suffix = String.format("%03d", (int) (Math.random() * 900) + 100);
        return String.format("%s-%s-%s", prefix, dept, suffix);
    }

    public Template getDefaultTemplate() {
        return templateRepository.findByCode("DEFAULT")
                .orElseGet(() -> templateRepository.save(Template.builder()
                        .code("DEFAULT")
                        .name("Blue Vertical")
                        .organizationName("ID Card Authority")
                        .layout("VERTICAL")
                        .primaryColor("#1d4ed8")
                        .secondaryColor("#dbeafe")
                        .textColor("#111827")
                        .tagline("Official Identification Card")
                        .build()));
    }
}
