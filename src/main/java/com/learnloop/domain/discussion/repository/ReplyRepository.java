package com.learnloop.domain.discussion.repository;

import com.learnloop.domain.discussion.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, String> {

    @Query("SELECT r FROM Reply r JOIN FETCH r.user WHERE r.question.id = :questionId ORDER BY r.createdAt ASC")
    List<Reply> findByQuestionIdWithUser(@Param("questionId") String questionId);

    long countByQuestionId(String questionId);
}
