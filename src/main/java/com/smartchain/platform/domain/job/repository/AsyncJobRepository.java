package com.smartchain.platform.domain.job.repository;

import com.smartchain.platform.domain.job.entity.AsyncJob;
import com.smartchain.platform.global.enums.JobStatus;
import com.smartchain.platform.global.enums.JobType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {

    Optional<AsyncJob> findByJobId(String jobId);

    List<AsyncJob> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<AsyncJob> findByStatusOrderByCreatedAtAsc(JobStatus status);

    List<AsyncJob> findByJobTypeAndStatusOrderByCreatedAtAsc(JobType jobType, JobStatus status);

    long countByRequesterIdAndStatus(Long requesterId, JobStatus status);
}
