package com.hotelbooking.controller;

import com.hotelbooking.dto.RoomTypeForm;
import com.hotelbooking.exception.BusinessException;
import com.hotelbooking.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/room-types")
public class RoomTypeAdminController {

    private final RoomTypeService roomTypeService;

    public RoomTypeAdminController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roomTypes", roomTypeService.findAll());
        return "admin/room-types";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("roomTypeForm", new RoomTypeForm());
        model.addAttribute("editing", false);
        return "admin/room-type-form";
    }

    @PostMapping
    public String create(@Valid RoomTypeForm roomTypeForm, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editing", false);
            return "admin/room-type-form";
        }
        try {
            roomTypeService.create(roomTypeForm.getName(), roomTypeForm.getDescription());
            redirectAttributes.addFlashAttribute("success", "Room type added.");
            return "redirect:/admin/room-types";
        } catch (BusinessException exception) {
            bindingResult.rejectValue("name", "duplicate", exception.getMessage());
            model.addAttribute("editing", false);
            return "admin/room-type-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("roomTypeForm", new RoomTypeForm(roomTypeService.getById(id)));
        model.addAttribute("roomTypeId", id);
        model.addAttribute("editing", true);
        return "admin/room-type-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid RoomTypeForm roomTypeForm,
                         BindingResult bindingResult, Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomTypeId", id);
            model.addAttribute("editing", true);
            return "admin/room-type-form";
        }
        try {
            roomTypeService.update(id, roomTypeForm.getName(), roomTypeForm.getDescription());
            redirectAttributes.addFlashAttribute("success", "Room type updated.");
            return "redirect:/admin/room-types";
        } catch (BusinessException exception) {
            bindingResult.rejectValue("name", "duplicate", exception.getMessage());
            model.addAttribute("roomTypeId", id);
            model.addAttribute("editing", true);
            return "admin/room-type-form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomTypeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Room type deleted.");
        } catch (BusinessException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/room-types";
    }
}