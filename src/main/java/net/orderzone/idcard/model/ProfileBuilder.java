package net.orderzone.idcard.model;

import java.time.LocalDate;
import java.util.UUID;

public final class ProfileBuilder {

    public static Profile createDefaultStudent(String fullName, String department, String title) {
        return createTemplate(ProfileType.STUDENT, fullName, department, title);
    }

    public static Profile createDefaultEmployee(String fullName, String department, String title) {
        return createTemplate(ProfileType.EMPLOYEE, fullName, department, title);
    }

    public static Profile createDefaultUser(String fullName) {
        return createTemplate(ProfileType.USER, fullName, null, null);
    }

    private static Profile createTemplate(ProfileType type, String fullName, String department, String title) {
        Profile profile = Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber(generateRegistrationNumber(type, department))
                .type(type)
                .fullName(fullName)
                .department(department)
                .title(title)
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(2))
                .build();
        return profile;
    }

    private static String generateRegistrationNumber(ProfileType type, String department) {
        String prefix = type.name().substring(0, 3);
        String dept = department != null && !department.isBlank() ? department.toUpperCase().replaceAll("[^A-Z0-9]", "") : "GEN";
        int suffix = (int) (Math.random() * 900) + 100;
        return String.format("%s-%s-%03d", prefix, dept, suffix);
    }
}
