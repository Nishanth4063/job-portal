package com.nishanth.jobportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.nishanth.jobportal.entity.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = "postedBy")
    List<Job> findAll();

    @EntityGraph(attributePaths = "postedBy")
    List<Job> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = "postedBy")
    List<Job> findByLocationContainingIgnoreCase(String location);

    @EntityGraph(attributePaths = "postedBy")
    List<Job> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(String title, String location);

    @EntityGraph(attributePaths = "postedBy")
    List<Job> findByPostedById(Long recruiterId);
}