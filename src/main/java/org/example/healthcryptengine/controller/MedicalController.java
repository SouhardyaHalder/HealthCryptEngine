package org.example.healthcryptengine.controller;

import jakarta.servlet.http.HttpSession;
import org.example.healthcryptengine.model.MedicalRecord;
import org.example.healthcryptengine.model.User;
import org.example.healthcryptengine.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MedicalController {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @GetMapping("/medical")
    public String showMedicalPage(Model model, HttpSession session) {
        User loggedInUser = (User)session.getAttribute("loggedInUser");
        List<MedicalRecord>records = medicalRecordRepository.findByUserId(loggedInUser.getId());
        model.addAttribute("records", records);
        model.addAttribute("totalRecords", records.size());
        return "medical";
    }

    @GetMapping("/medical/add")
    public String showAddRecordPage() {
        return "medical-add";
    }
    @PostMapping("/medical/add")
    public String handleAddRecord(
            @RequestParam String patientName,
            @RequestParam int age,
            @RequestParam String bloodGroup,
            @RequestParam String doctorName,
            @RequestParam String diagnosis,
            @RequestParam String prescription,
            @RequestParam String dateOfVisit,
            HttpSession session) {

        MedicalRecord record = new MedicalRecord();
        record.setPatientName(patientName);
        record.setAge(age);
        record.setBloodGroup(bloodGroup);
        record.setDoctorName(doctorName);
        record.setDiagnosis(diagnosis);
        record.setPrescription(prescription);
        record.setDateOfVisit(LocalDate.parse(dateOfVisit));

        User loggedInUser =(User)session.getAttribute("loggedInUser");
        record.setId(loggedInUser.getId());
        medicalRecordRepository.save(record);
        return "redirect:/medical";
    }

    @GetMapping("/medical/view/{id}")
    public String viewRecord(@PathVariable Long id, Model model) {
        MedicalRecord record = medicalRecordRepository.findById(id).orElse(null);
        model.addAttribute("record", record);
        return "medical-view";
    }

    @GetMapping("/medical/delete/{id}")
    public String deleteRecord(@PathVariable Long id){
        medicalRecordRepository.deleteById(id);
        return "redirect:/medical";
    }
}
