package net.orderzone.idcard.controller;

import net.orderzone.idcard.model.Template;
import net.orderzone.idcard.service.TemplateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("templates", templateService.listAll());
        return "templates/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("template", new Template());
        return "templates/form";
    }

    @PostMapping
    public String create(@ModelAttribute @Valid Template template, BindingResult result) {
        if (result.hasErrors()) {
            return "templates/form";
        }
        templateService.save(template);
        return "redirect:/templates";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Template template = templateService.findById(id);
        model.addAttribute("template", template);
        return "templates/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        templateService.delete(id);
        return "redirect:/templates";
    }
}
