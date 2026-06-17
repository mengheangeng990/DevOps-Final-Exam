package net.orderzone.idcard.controller;

import net.orderzone.idcard.model.Profile;
import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.service.ProfileService;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final TemplateService templateService;

    public ProfileController(ProfileService profileService, TemplateService templateService) {
        this.profileService = profileService;
        this.templateService = templateService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("profiles", profileService.listAll());
        return "profiles/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("templates", templateService.listAll());
        return "profiles/form";
    }

    @PostMapping
    public String create(@ModelAttribute @Valid Profile profile, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("templates", templateService.listAll());
            return "profiles/form";
        }
        profileService.save(profile);
        return "redirect:/profiles";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Profile profile = profileService.findById(id).orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        model.addAttribute("profile", profile);
        model.addAttribute("templates", templateService.listAll());
        return "profiles/form";
    }

    @PostMapping("/upload/{id}")
    public String uploadPhoto(@PathVariable Long id, @RequestParam("photo") MultipartFile photo, Model model) throws IOException {
        profileService.uploadPhoto(id, photo);
        return "redirect:/profiles/edit/" + id;
    }

    @GetMapping("/preview/{id}")
    public String preview(@PathVariable Long id, Model model) throws IOException {
        Profile profile = profileService.findById(id).orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        model.addAttribute("profile", profile);
        model.addAttribute("template", profile.getTemplate() != null ? profile.getTemplate() : profileService.getDefaultTemplate());
        return "profiles/preview";
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) throws IOException {
        Profile profile = profileService.findById(id).orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        byte[] bytes = profileService.getPhotoBytes(profile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, profile.getPhotoContentType())
                .body(bytes);
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        profileService.delete(id);
        return "redirect:/profiles";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<Profile> results = profileService.searchByName(query);
        model.addAttribute("profiles", results);
        return "profiles/list";
    }
}
