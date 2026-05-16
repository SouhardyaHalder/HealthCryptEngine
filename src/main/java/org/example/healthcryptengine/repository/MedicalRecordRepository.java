package org.example.healthcryptengine.repository;

import org.example.healthcryptengine.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Get all records for a specific user
    List<MedicalRecord> findByUserId(Long userId);

    // Search by doctor name
    List<MedicalRecord> findByDoctorName(String doctorName);

    // Search by diagnosis
    List<MedicalRecord> findByDiagnosis(String diagnosis);
}