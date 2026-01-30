package com.elearning.repository;

import com.elearning.model.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    List<PollVote> findByPollId(String pollId);
    
    @Query("SELECT COUNT(pv) FROM PollVote pv WHERE pv.poll.id = :pollId")
    Long countVotesByPollId(@Param("pollId") String pollId);
    
    @Query("SELECT pv FROM PollVote pv WHERE pv.poll.id = :pollId AND pv.user.id = :userId")
    PollVote findByPollIdAndUserId(@Param("pollId") String pollId, @Param("userId") Long userId);
}
